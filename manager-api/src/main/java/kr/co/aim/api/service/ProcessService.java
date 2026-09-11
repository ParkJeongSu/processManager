package kr.co.aim.api.service;

import kr.co.aim.common.enums.SystemName;
import kr.co.aim.common.condition.ProcessControlRequestCondition;
import kr.co.aim.domain.command.ProcessStatusCreateCommand;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.domain.repository.ProcessStatusHistoryRepository;
import kr.co.aim.domain.repository.ProcessStatusRepository;
import kr.co.aim.infra.persistence.mapper.ProcessStatusHistoryMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import kr.co.aim.api.dto.ProcessStatusResponseDto;
import kr.co.aim.common.enums.ProcessState;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.domain.model.ProcessStatusHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessService {
    private final ProcessStatusService processStatusService;
    private final ConnectionCheckService connectionCheckService;
    private final ProcessAsyncService processAsyncService;
    private final ProcessStatusHistoryMapper processStatusHistoryMapper;
    private final ProcessStatusHistoryRepository processStatusHistoryRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessInfoRepository processInfoRepository;

    public List<ProcessStatusResponseDto> getProcessList() {
        List<ProcessStatusResponseDto> resultList = new ArrayList<>();
        List<ProcessInfo> processes = processInfoRepository.findAll();
        List<ProcessStatus> processStatuses = processStatusRepository.findAll();
        Map<Integer, ProcessStatus> statusPorts = new HashMap<>();
        for (ProcessStatus status : processStatuses) {
            statusPorts.put(status.getPort(), status);
        }
        for (ProcessInfo p : processes) {
            boolean isExist = statusPorts.containsKey(p.getPort());
            if (isExist) {
                ProcessStatus ps = statusPorts.get(p.getPort());
                ProcessStatusResponseDto dto = ProcessStatusResponseDto.builder()
                        .port(ps.getPort())
                        .systemName(p.getSystemName())
                        .processGroupName(p.getProcessGroupName())
                        .processName(p.getProcessName())
                        .pid(ps.getPid())
                        .status(ps.getStatus())
                        .description(p.getDescription())
                        .build();

                if (ProcessState.STARTING.getValue().equals(ps.getStatus())) {
                    dto.setStartRequestTime(ps.getStartRequestTime());
                } else if (ProcessState.RUNNING.getValue().equals(ps.getStatus())) {
                    dto.setStartTime(ps.getStartTime());
                } else if (ProcessState.DOWN.getValue().equals(ps.getStatus())) {
                    dto.setEndTime(ps.getEndTime());
                } else if (ProcessState.STOPPING.getValue().equals(ps.getStatus())) {
                    dto.setEndRequestTime(ps.getEndRequestTime());
                }
                resultList.add(dto);
            } else {
                ProcessStatusResponseDto dto = ProcessStatusResponseDto.builder()
                        .port(p.getPort())
                        .systemName(p.getSystemName())
                        .processGroupName(p.getProcessGroupName())
                        .processName(p.getProcessName())
                        .description(p.getDescription())
                        .status(ProcessState.DOWN.getValue())
                        .build();
                resultList.add(dto);
            }
        }

        // Gal
        boolean galStatus = connectionCheckService.getGalDbStatus();
        ProcessStatusResponseDto galDto = new ProcessStatusResponseDto();
        galDto.setSystemName(SystemName.GAL.getValue());
        galDto.setProcessGroupName(SystemName.GAL.getValue());
        galDto.setProcessName(SystemName.GAL.getValue());
        galDto.setStatus(galStatus ? ProcessState.RUNNING.getValue() : ProcessState.DOWN.getValue());
        resultList.add(galDto);

        // Manti
        boolean mantiStatus = connectionCheckService.getMantiStatus();
        ProcessStatusResponseDto mantiDto = new ProcessStatusResponseDto();
        mantiDto.setSystemName(SystemName.MANTI.getValue());
        mantiDto.setProcessGroupName(SystemName.MANTI.getValue());
        mantiDto.setProcessName(SystemName.MANTI.getValue());
        mantiDto.setStatus(mantiStatus ? ProcessState.RUNNING.getValue() : ProcessState.DOWN.getValue());
        resultList.add(mantiDto);

        return resultList;
    }

    /**
     * 포트 소켓 응답 검사 (TIME_WAIT 방지 및 타임아웃 500ms 설정)
     */
    private boolean isPortOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.setSoLinger(true, 0);
            socket.connect(new InetSocketAddress("127.0.0.1", port), 500);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 대상 프로세스 식별 (파일명 및 Java 프로세스 안전 대조)
     */
    private boolean isTargetProcess(long pid, String expectedFileName) {
        if (pid <= 0 || StringUtils.isBlank(expectedFileName)) {
            return false;
        }

        Optional<ProcessHandle> ph = ProcessHandle.of(pid);
        if (ph.isEmpty() || !ph.get().isAlive()) {
            return false;
        }

        ProcessHandle.Info info = ph.get().info();
        String target = expectedFileName.toLowerCase();

        // 1. 전체 커맨드라인(인자 포함) 검사
        if (info.commandLine().isPresent()) {
            if (info.commandLine().get().toLowerCase().contains(target)) {
                return true;
            }
        }

        // 2. 실행 바이너리 검사
        if (info.command().isPresent()) {
            String command = info.command().get().toLowerCase();
            if (command.contains(target)) {
                return true;
            }
            // 3. .jar 파일인데 권한 문제로 commandLine을 못 가져온 경우 java.exe 검증으로 Fallback
            if (target.endsWith(".jar") && command.contains("java.exe")) {
                return true;
            }
        }

        return false;
    }

    /**
     * 프로세스 시작 로직
     */
    @Transactional
    public ProcessStatusResponseDto startProcess(int port, ProcessControlRequestCondition vo) {
        Optional<ProcessInfo> optionalProcessInfo = processInfoRepository.findByPort(port);
        if (optionalProcessInfo.isEmpty()) {
            throw new IllegalArgumentException("해당 포트(" + port + ")의 프로세스 설정 정보가 없습니다.");
        }
        ProcessInfo processInfo = optionalProcessInfo.get();

        if (isPortOpen(port)) {
            throw new IllegalStateException("이미 실행 중인 프로세스입니다. (Port: " + port + ")");
        }
        LocalDateTime currentTime = LocalDateTime.now();

        Optional<ProcessStatus> optionalProcessStatus = processStatusRepository.findByPort(port);
        ProcessStatus processStatus = null;
        if (optionalProcessStatus.isPresent()) {
            processStatus = optionalProcessStatus.get();
            if (processStatus.getStatus().equals(ProcessState.STARTING.getValue())) {
                throw new IllegalStateException("이미 실행 중인 프로세스입니다. (Port: " + port + ")");
            } else if (processStatus.getStatus().equals(ProcessState.STOPPING.getValue())) {
                throw new IllegalStateException("이미 종료 중인 프로세스입니다. (Port: " + port + ")");
            }
        }
        if (optionalProcessStatus.isEmpty()) {
            ProcessStatusCreateCommand command = ProcessStatusCreateCommand.builder()
                    .port(port)
                    .processName(processInfo.getProcessName())
                    .lastEventUser(vo.getEventUser())
                    .build();
            processStatus = ProcessStatus.create(command);
        }

        processStatus.setStatus(ProcessState.STARTING.getValue());
        processStatus.setStartRequestTime(currentTime);
        processStatus = processStatusRepository.save(processStatus);

        ProcessStatusHistory processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
        processStatusHistoryRepository.save(processStatusHistory);

        try {
            // 파일 복사 로직
            String copyDirStr = processInfo.getCopyDir();
            String workDirStr = processInfo.getWorkingDir();
            String fileName = processInfo.getFileName();

            if (StringUtils.isNotBlank(copyDirStr)) {
                Path sourcePath = Paths.get(copyDirStr, fileName);
                Path targetPath = Paths.get(workDirStr, fileName);
                File workingDirectory = new File(workDirStr);

                if (!Files.exists(sourcePath)) {
                    throw new IOException("원본 파일이 존재하지 않습니다: " + sourcePath);
                }
                if (!workingDirectory.exists()) {
                    workingDirectory.mkdirs();
                }
                if (!StringUtils.equals(copyDirStr, workDirStr)) {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("File Copied: {} -> {}", sourcePath, targetPath);
                }
            }

            // 프로세스 실행
            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("start");
            commands.add("/b");
            String batchFullPath = processInfo.getBatchDir() + File.separator + processInfo.getBatchName();
            commands.add(batchFullPath);

            if (StringUtils.equals(SystemName.MNG.getValue(), processInfo.getSystemName())) {
                commands.add(processInfo.getWorkingDir());
                commands.add(processInfo.getFileName());
                log.info("MNG System detected. Arguments added: {} {}", processInfo.getWorkingDir(), processInfo.getFileName());
            } else {
                log.info("General System detected. Executing batch without extra arguments.");
            }

            ProcessBuilder pb = new ProcessBuilder(commands);
            File batchDirectory = new File(processInfo.getBatchDir());
            if (!batchDirectory.exists()) {
                batchDirectory.mkdirs();
            }
            pb.directory(batchDirectory);

            File logFile = new File(processInfo.getBatchDir(), "batch_exec.log");
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));

            pb.start();
            log.info("Process Started Command Sent: {}", processInfo.getProcessName());

            return ProcessStatusResponseDto.builder()
                    .port(port)
                    .systemName(processInfo.getSystemName())
                    .processGroupName(processInfo.getProcessGroupName())
                    .processName(processInfo.getProcessName())
                    .status(ProcessState.STARTING.getValue())
                    .startRequestTime(processStatus.getStartRequestTime())
                    .description("시작 명령 전송 완료")
                    .build();

        } catch (IOException e) {
            log.error("Process Start Failed", e);
            throw new RuntimeException("프로세스 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 프로세스 종료 로직
     */
    @Transactional
    public ProcessStatusResponseDto stopProcess(int port, ProcessControlRequestCondition requestVo) {
        if (!isPortOpen(port)) {
            throw new IllegalStateException("이미 종료된 프로세스이거나 연결할 수 없습니다.");
        }

        Optional<ProcessInfo> optionalProcessInfo = processInfoRepository.findByPort(port);
        if (optionalProcessInfo.isEmpty()) {
            throw new IllegalArgumentException("설정 정보 없음");
        }
        ProcessInfo processInfo = optionalProcessInfo.get();

        processStatusService.checkStoppingStatus(port);

        // ======================= [무중단 패치 검증 로직 시작] =======================
        String currentGroup = processInfo.getProcessGroupName();

        // processGroupName이 설정되어 있는 경우에만 그룹 체크 수행
        if (currentGroup != null && !currentGroup.trim().isEmpty()) {
            List<ProcessInfo> allProcessList = processInfoRepository.findAll();

            // 1. 현재 프로세스와 동일한 그룹을 가진 프로세스 목록 수집
            List<ProcessInfo> sameGroupProcesses = new ArrayList<>();
            for (ProcessInfo item : allProcessList) {
                if (item.getPort() != null && currentGroup.equals(item.getProcessGroupName())) {
                    sameGroupProcesses.add(item);
                }
            }

            // 2. 동일 그룹 프로세스가 2개 이상일 때만 다른 프로세스들의 상태 검사
            if (sameGroupProcesses.size() > 1) {
                boolean hasOtherRunningProcess = false;

                for (ProcessInfo groupItem : sameGroupProcesses) {
                    // 본인 포트는 검사 대상에서 제외
                    if (groupItem.getPort().equals(port)) {
                        continue;
                    }

                    // 다른 프로세스의 상태 조회
                    Optional<ProcessStatus> otherStatusOpt = processStatusRepository.findByPort(groupItem.getPort());
                    if (otherStatusOpt.isPresent()) {
                        ProcessStatus otherStatus = otherStatusOpt.get();
                        // 실행 중(Running) 상태인 다른 프로세스가 있는지 확인
                        if (ProcessState.RUNNING.getValue().equals(otherStatus.getStatus())) {
                            hasOtherRunningProcess = true;
                            break; // 하나라도 실행 중인 것을 확인했으므로 탐색 종료
                        }
                    }
                }

                // 본인 외에 Running 중인 프로세스가 하나도 없다면 종료 차단
                if (!hasOtherRunningProcess) {
                    throw new IllegalStateException(
                            "해당 프로세스 그룹(" + currentGroup + ")에서 현재 실행 중인 마지막 프로세스이므로 무중단 패치를 위해 종료할 수 없습니다."
                    );
                }
            }
        }
        // ======================= [무중단 패치 검증 로직 끝] =======================

        try {
            processStatusService.markAsStopping(port, processInfo, requestVo.getEventUser());
        } catch (Exception e) {
            log.error("상태 업데이트(STOPPING) 실패 했으나 프로세스 종료는 계속 진행함", e);
        }

        try {
            if (StringUtils.isNotBlank(processInfo.getStopBatchName())) {
                List<String> commands = new ArrayList<>();
                commands.add("cmd.exe");
                commands.add("/c");
                commands.add("start");
                commands.add("/b");
                String batchFullPath = processInfo.getBatchDir() + File.separator + processInfo.getStopBatchName();
                commands.add(batchFullPath);

                ProcessBuilder pb = new ProcessBuilder(commands);
                File batchDirectory = new File(processInfo.getBatchDir());
                if (!batchDirectory.exists()) {
                    batchDirectory.mkdirs();
                }
                pb.directory(batchDirectory);

                File logFile = new File(processInfo.getBatchDir(), "batch_exec.log");
                pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
                pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));

                pb.start();
            } else {
                String url = "http://localhost:" + port + "/wcs-web" + "/api/v1/application/shutdown";
                log.info("종료 요청 전송: {}", url);
                processAsyncService.performShutdown(port, url, requestVo);
                log.info("Process Stop Command Sent: {}", processInfo.getProcessName());
            }

            LocalDateTime currentTime = LocalDateTime.now();

            return ProcessStatusResponseDto.builder()
                    .port(port)
                    .systemName(processInfo.getSystemName())
                    .processGroupName(processInfo.getProcessGroupName())
                    .processName(processInfo.getProcessName())
                    .status(ProcessState.STOPPING.getValue())
                    .endTime(currentTime)
                    .description("종료 명령 전송 완료")
                    .build();

        } catch (ResourceAccessException e) {
            throw new IllegalStateException("프로세스에 연결할 수 없습니다. 이미 종료되었을 수 있습니다.");
        } catch (Exception e) {
            log.error("Stop Failed", e);
            throw new RuntimeException("종료 요청 실패: " + e.getMessage());
        }
    }

    /**
     * 현재 윈도우 OS에서 LISTENING 중인 모든 TCP 포트와 PID 매핑을 1회 일괄 수집
     */
    private Map<Integer, Long> getListeningPortPidMap() {
        Map<Integer, Long> portPidMap = new HashMap<>();
        ProcessBuilder pb = new ProcessBuilder("netstat", "-ano", "-p", "tcp");

        try {
            Process process = pb.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("TCP") && line.contains("LISTENING")) {
                        String[] tokens = line.split("\\s+");
                        String localAddress = tokens[1];
                        int colonIndex = localAddress.lastIndexOf(':');

                        if (colonIndex != -1) {
                            try {
                                String portStr = localAddress.substring(colonIndex + 1);
                                int port = Integer.parseInt(portStr.replaceAll("[^0-9]", ""));
                                long pid = Long.parseLong(tokens[tokens.length - 1]);
                                portPidMap.put(port, pid);
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("netstat 실행 중 오류 발생", e);
        }

        return portPidMap;
    }

    @Transactional
    public void checkProcessStatus() {
        List<ProcessInfo> processInfoList = processInfoRepository.findAll();
        List<ProcessStatus> processStatusList = processStatusRepository.findAll();

        Map<Integer, ProcessStatus> statusPorts = new HashMap<>();
        for (ProcessStatus status : processStatusList) {
            statusPorts.put(status.getPort(), status);
        }

        LocalDateTime currentTime = LocalDateTime.now();
        long gracePeriodMinutes = 3;

        // 루프 밖에서 netstat 1회 실행
        Map<Integer, Long> currentListeningMap = getListeningPortPidMap();

        for (ProcessInfo processInfo : processInfoList) {
            int port = processInfo.getPort();
            Long activePid = currentListeningMap.get(port);

            // 1) netstat 포트 점유 여부
            boolean isListening = (activePid != null);
            // 2) 실제 TCP 응답성(Hang 여부) 검증
            boolean isPortUp = isListening && isPortOpen(port);
            // 3) 프로그램 파일명 일치 여부 검증
            boolean isGenuineProcess = isPortUp && isTargetProcess(activePid, processInfo.getFileName());

            Long validPid = isGenuineProcess ? activePid : null;

            ProcessStatus processStatus = statusPorts.get(port);
            ProcessStatusHistory processStatusHistory = null;

            if (ObjectUtils.isEmpty(processStatus)) {
                ProcessStatusCreateCommand command = ProcessStatusCreateCommand.builder()
                        .port(port)
                        .processName(processInfo.getProcessName())
                        .build();
                processStatus = ProcessStatus.create(command);

                if (isGenuineProcess) {
                    processStatus.setStatus(ProcessState.RUNNING.getValue());
                    processStatus.setStartTime(currentTime);
                    processStatus.setPid(validPid.intValue());
                    processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                    log.info("[{}] 기동 확인 -> RUNNING 전환 (PID: {})", processStatus.getProcessName(), validPid);
                } else {
                    processStatus.setStatus(ProcessState.DOWN.getValue());
                    processStatus.setEndTime(currentTime);
                    processStatus.setPid(null);
                    processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                    log.info("[{}] 정지 확인 -> DOWN 전환", processStatus.getProcessName());
                }
            } else {
                String dbStatus = processStatus.getStatus();

                // -------------------------------------------------------
                // CASE A: 켜지는 중 (STARTING)
                // -------------------------------------------------------
                if (ProcessState.STARTING.getValue().equals(dbStatus)) {
                    if (isGenuineProcess) {
                        processStatus.setStatus(ProcessState.RUNNING.getValue());
                        processStatus.setStartTime(currentTime);
                        processStatus.setPid(validPid.intValue());
                        log.info("[{}] 정상 기동 확인 -> RUNNING 전환 (PID: {})", processStatus.getProcessName(), validPid);
                        processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                    } else {
                        LocalDateTime requestTime = processStatus.getStartRequestTime();
                        if (requestTime != null && requestTime.plusMinutes(gracePeriodMinutes).isBefore(currentTime)) {
                            processStatus.setStatus(ProcessState.DOWN.getValue());
                            processStatus.setEndTime(currentTime);
                            processStatus.setPid(null);
                            log.error("[{}] 기동 시간 초과 (3분 경과) -> DOWN 처리", processStatus.getProcessName());
                            processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                        }
                    }
                }
                // -------------------------------------------------------
                // CASE B: 꺼지는 중 (STOPPING)
                // -------------------------------------------------------
                else if (ProcessState.STOPPING.getValue().equals(dbStatus)) {
                    if (!isPortUp) {
                        processStatus.setStatus(ProcessState.DOWN.getValue());
                        processStatus.setEndTime(currentTime);
                        processStatus.setPid(null);
                        log.info("[{}] 정상 종료 확인 -> DOWN 전환", processStatus.getProcessName());
                        processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                    } else {
                        LocalDateTime requestTime = processStatus.getEndRequestTime();
                        if (requestTime != null && requestTime.plusMinutes(gracePeriodMinutes).isBefore(currentTime)) {
                            processStatus.setStatus(ProcessState.RUNNING.getValue());
                            processStatus.setPid(activePid.intValue());
                            log.warn("[{}] 종료 실패 타임아웃 -> RUNNING 유지", processStatus.getProcessName());
                            processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                        }
                    }
                }
                // -------------------------------------------------------
                // CASE C: 실행 중이어야 함 (RUNNING)
                // -------------------------------------------------------
                else if (ProcessState.RUNNING.getValue().equals(dbStatus)) {
                    if (!isGenuineProcess) {
                        log.error("[{}] 비정상 종료 감지! (응답 없음 또는 타깃 불일치)", processStatus.getProcessName());
                        processStatus.setStatus(ProcessState.DOWN.getValue());
                        processStatus.setEndTime(currentTime);
                        processStatus.setPid(null);
                        processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                    } else {
                        if (processStatus.getPid() == null || processStatus.getPid() != validPid.intValue()) {
                            processStatus.setPid(validPid.intValue());
                        }
                    }
                }
                // -------------------------------------------------------
                // CASE D: 꺼져 있어야 함 (DOWN) -> 외부에서 임의 기동 시
                // -------------------------------------------------------
                else if (ProcessState.DOWN.getValue().equals(dbStatus)) {
                    if (isGenuineProcess) {
                        log.info("[{}] 외부 기동 감지 -> RUNNING 동기화 (PID: {})", processStatus.getProcessName(), validPid);
                        processStatus.setStatus(ProcessState.RUNNING.getValue());
                        processStatus.setStartTime(currentTime);
                        processStatus.setPid(validPid.intValue());
                        processStatusHistory = processStatusHistoryMapper.toHistoryEntity(processStatus);
                    }
                }
            }

            if (ObjectUtils.isNotEmpty(processStatusHistory)) {
                processStatusHistoryRepository.save(processStatusHistory);
            }
            processStatusRepository.save(processStatus);
        }
    }
}