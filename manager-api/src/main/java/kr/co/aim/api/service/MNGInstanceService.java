package kr.co.aim.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import kr.co.aim.common.dto.ProcessStatusResponseDto;
import kr.co.aim.common.dto.ProcessControlRequestDto;
import kr.co.aim.common.enums.ProcessState;
import kr.co.aim.common.enums.SystemName;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.domain.model.ProcessStatusHistory;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.domain.repository.ProcessStatusHistoryRepository;
import kr.co.aim.domain.repository.ProcessStatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class MNGInstanceService {
    private final ProcessInfoRepository processInfoRepository;
    private final ProcessStatusRepository processStatusRepository;
    private final ProcessStatusHistoryRepository processStatusHistoryRepository;

    @Autowired
    @Lazy
    private MNGInstanceService mngInstanceService;
    // HTTP 요청을 보내기 위한 도구 (Spring 기본 제공)
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ProcessStatusResponseDto> getProcessList(){
        List<ProcessStatusResponseDto> resultList = new ArrayList<>();

        List<ProcessInfo> processes = processInfoRepository.findBySystemName(SystemName.MNG.getValue());
        List<ProcessStatus> processStatuses = processStatusRepository.findAll();
        Map<Integer,ProcessStatus> statusPorts = new HashMap<>();
        for (ProcessStatus status : processStatuses) {
            statusPorts.put(status.getPort(),status);
        }
        for(ProcessInfo p : processes){
            boolean isExist = statusPorts.containsKey(p.getPort());
            if(isExist){
                ProcessStatus ps = statusPorts.get(p.getPort());
                ProcessStatusResponseDto dto = new ProcessStatusResponseDto();
                dto.setPort(p.getPort());
                dto.setSystemName(p.getSystemName());
                dto.setProcessGroupName(p.getProcessGroupName());
                dto.setProcessName(p.getProcessName());
                dto.setPid(ps.getPid());
                dto.setStatus(ps.getStatus());
                dto.setDescription(p.getDescription());

                if(ProcessState.STARTING.getValue().equals(ps.getStatus())){
                    dto.setStartRequestTime(ps.getStartRequestTime());
                } else if(ProcessState.RUNNING.getValue().equals(ps.getStatus())){
                    dto.setStartTime(ps.getStartTime());
                } else if(ProcessState.DOWN.getValue().equals(ps.getStatus())){
                    dto.setEndTime(ps.getEndTime());
                } else if (ProcessState.STOPPING.getValue().equals(ps.getStatus())) {
                    dto.setEndRequestTime(ps.getEndRequestTime());
                }
                resultList.add(dto);
            }
            else {
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

        return resultList;
    }

    // 상태 체크: 해당 포트가 열려있는지 확인
    private boolean isRunning(int port) {
        // try-with-resources (Java 7+) 자동 close
        try (Socket socket = new Socket("localhost", port)) {
            return true; // 접속 성공 = 프로세스 떠있음
        } catch (IOException e) {
            return false; // 접속 실패 = 프로세스 죽어있음
        }
    }

    // PID로 프로세스 생존 확인
    private boolean isProcessAlive(long pid) {
        if (pid <= 0) return false;
        return ProcessHandle.of(pid).isPresent(); // OS에 해당 PID가 있는지 확인
    }

    // [핵심] netstat 명령어로 포트를 쓰는 PID 찾기
    private String findPidByPort(int port) {
        try {
            // 윈도우 명령어: netstat -ano | findstr :포트번호
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", "netstat -ano | findstr :" + port);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                // 결과 예시: "  TCP    0.0.0.0:8081           0.0.0.0:0              LISTENING       12345"
                // 맨 마지막 숫자가 PID 입니다.
                if (line.contains("LISTENING")) {
                    String[] parts = line.trim().split("\\s+"); // 공백으로 쪼개기
                    return parts[parts.length - 1]; // 마지막 요소가 PID
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
        }
        return null; // 못 찾음
    }

    // [유틸] 포트 체크 (Java Socket 이용 - 매우 빠름)
    private boolean isPortOpen(int port) {
        try (Socket socket = new Socket("127.0.0.1", port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    /**
     * 프로세스 시작 로직
     */
    @Transactional
    public ProcessStatusResponseDto startProcess(int port, ProcessControlRequestDto requestDto) {

        ProcessInfo processInfo = processInfoRepository.findByPort(port)
                .orElseThrow(() -> new IllegalArgumentException("해당 포트(" + port + ")의 프로세스 설정 정보가 없습니다."));

        // 1. 방어 로직
        if (isRunning(port)) {
            throw new IllegalStateException("이미 실행 중인 프로세스입니다. (Port: " + port + ")");
        }

        // 1.1 방어로직 스케줄러가 State를 바꾼후 시작할 수 있게끔
        Optional<ProcessStatus> optionalProcessStatus = processStatusRepository.findByPort(port);
        if(optionalProcessStatus.isPresent()){
            ProcessStatus processStatus = optionalProcessStatus.get();
            if(processStatus.getStatus().equals(ProcessState.STARTING.getValue())){
                throw new IllegalStateException("이미 실행 중인 프로세스입니다. (Port: " + port + ")");
            }
            else if(processStatus.getStatus().equals(ProcessState.STOPPING.getValue())){
                throw new IllegalStateException("이미 종료 중인 프로세스입니다. (Port: " + port + ")");
            }
        }


        try {
            // 2-1. 파일 복사
            String copyDirStr = processInfo.getCopyDir();
            String workDirStr = processInfo.getWorkingDir();
            String fileName   = processInfo.getFileName();

            Path sourcePath = Paths.get(copyDirStr, fileName);
            Path targetPath = Paths.get(workDirStr, fileName);
            File workingDirectory = new File(workDirStr);

            if (!Files.exists(sourcePath)) {
                throw new IOException("원본 파일이 존재하지 않습니다: " + sourcePath);
            }

            if (!workingDirectory.exists()) {
                workingDirectory.mkdirs();
            }

            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File Copied: {} -> {}", sourcePath, targetPath);

            // 2-2. 프로세스 실행
            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("start");
            commands.add("/b");

            commands.add(processInfo.getCommand()); // "javaw"
            commands.add("-jar");                   // "-jar" 필수
            commands.add(fileName);                 // "mng.jar"

            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.directory(workingDirectory);
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);

            pb.start();

            LocalDateTime currentTime = LocalDateTime.now();

            // 3. DB 상태 업데이트 (STARTING)
            ProcessStatus ps = processStatusRepository.findByPort(port)
                    .orElse(new ProcessStatus());

            // 신규 엔티티일 경우 키 세팅
            if (ps.getPort() == null) {
                ps.setPort(port);
                ps.setProcessName(processInfo.getProcessName());
            }

            ps.setStatus(ProcessState.STARTING.getValue());
            ps.setStartRequestTime(currentTime);
            // 재시작일 수 있으니 이전 종료 시간 등은 초기화하지 않음 (정책에 따라 결정)

            processStatusRepository.save(ps);

            ProcessStatusHistory processStatusHistory =
                    ProcessStatusHistory.builder()
                            .eventTime(currentTime)
                            .port(port)
                            .processName(processInfo.getProcessName())
                            .status(ProcessState.STARTING.getValue())
                            .startRequestTime(currentTime)
                            .eventUser(requestDto.getEventUser())
                            .build();
            processStatusHistoryRepository.save(processStatusHistory);

            log.info("Process Started Command Sent: {}", processInfo.getProcessName());

            // 4. 결과 DTO 반환
            return ProcessStatusResponseDto.builder()
                    .port(port)
                    .systemName(processInfo.getSystemName())
                    .processGroupName(processInfo.getProcessGroupName())
                    .processName(processInfo.getProcessName())
                    .status(ProcessState.STARTING.getValue())
                    .startRequestTime(ps.getStartRequestTime())
                    .description("시작 명령 전송 완료")
                    .build();

        } catch (IOException e) {
            log.error("Process Start Failed", e);
            throw new RuntimeException("프로세스 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 프로세스 상태를 '종료 중(STOPPING)'으로 변경하고 히스토리를 남김
     * 독립적인 트랜잭션으로 즉시 커밋됨 (Propagation.REQUIRES_NEW)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsStopping(int port, ProcessInfo processInfo, String eventUser) {
        LocalDateTime currentTime = LocalDateTime.now();

        // 1. ProcessStatus 조회 및 업데이트
        ProcessStatus ps = processStatusRepository.findByPort(port)
                .orElse(new ProcessStatus());

        if (ps.getPort() == null) {
            ps.setPort(port);
            ps.setProcessName(processInfo.getProcessName());
        }

        // STOPPING 상태로 변경 (ProcessState Enum에 STOPPING이 있다고 가정)
        ps.setStatus(ProcessState.STOPPING.getValue());
        ps.setEndRequestTime(currentTime); // 수정 시간 업데이트
        processStatusRepository.save(ps);

        // 2. History 추가
        ProcessStatusHistory history = ProcessStatusHistory.builder()
                .eventTime(currentTime)
                .port(port)
                .processName(processInfo.getProcessName())
                .status(ProcessState.STOPPING.getValue()) // 히스토리에도 STOPPING 기록
                .endRequestTime(currentTime)
                .eventUser(eventUser)
                .build();

        processStatusHistoryRepository.save(history);
    }

    /**
     * 프로세스 종료 로직
     */
    @Transactional
    public ProcessStatusResponseDto stopProcess(int port, ProcessControlRequestDto requestDto) {

        if (!isRunning(port)) {
            throw new IllegalStateException("이미 종료된 프로세스이거나 연결할 수 없습니다.");
        }

        ProcessInfo processInfo = processInfoRepository.findByPort(port)
                .orElseThrow(() -> new IllegalArgumentException("설정 정보 없음"));

        try {
            mngInstanceService.markAsStopping(port, processInfo, requestDto.getEventUser());
        } catch (Exception e) {
            log.error("상태 업데이트(STOPPING) 실패 했으나 프로세스 종료는 계속 진행함", e);
            // DB 업데이트 실패해도 실제 프로세스 종료는 시도할지 여부는 정책에 따라 결정
        }

        // url : graceful shutdown url
        String url = "http://localhost:" + port + "/stop";
        log.info("종료 요청 전송: {}", url);

        try {
            // 1. 종료 요청
            restTemplate.postForObject(url, null, String.class);

            LocalDateTime currentTime = LocalDateTime.now();

            // 2. DB 상태 업데이트 (STOPPING)
            // 실제 종료 확인은 스케줄러가 하겠지만, UI 반응성을 위해 STOPPING으로 변경
            ProcessStatus ps = processStatusRepository.findByPort(port)
                    .orElse(new ProcessStatus()); // 없을 리 없지만 방어코드

            if (ps.getPort() == null) {
                ps.setPort(port);
                ps.setProcessName(processInfo.getProcessName());
            }

            ps.setStatus(ProcessState.DOWN.getValue());
            ps.setEndTime(currentTime);
            processStatusRepository.save(ps);

            ProcessStatusHistory processStatusHistory =
                    ProcessStatusHistory.builder()
                            .eventTime(currentTime)
                            .port(port)
                            .processName(processInfo.getProcessName())
                            .status(ProcessState.DOWN.getValue())
                            .endTime(currentTime)
                            .eventUser(requestDto.getEventUser())
                            .build();
            processStatusHistoryRepository.save(processStatusHistory);

            log.info("Process Stop Command Sent: {}", processInfo.getProcessName());

            return ProcessStatusResponseDto.builder()
                    .port(port)
                    .systemName(processInfo.getSystemName())
                    .processGroupName(processInfo.getProcessGroupName())
                    .processName(processInfo.getProcessName())
                    .status(ProcessState.DOWN.getValue())
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

    @Transactional
    public void checkProcessStatus() {
        List<ProcessStatus> processList = processStatusRepository.findAll();

        LocalDateTime currentTime = LocalDateTime.now();

        for (ProcessStatus ps : processList) {

            ProcessStatusHistory processStatusHistory = null;
            String dbStatus = ps.getStatus();
            int port = ps.getPort();
            String processId = this.findPidByPort(port);
            Long pid = processId !=null ? Long.parseLong(processId) : 0L;
            Integer processIdByInteger = processId !=null ? Integer.parseInt(processId) :0;

            // 1. 현재 물리적 상태 체크
            boolean isPortUp = isPortOpen(port);
            boolean isPidAlive = isProcessAlive(pid);

            // -------------------------------------------------------
            // CASE A: 켜지는 중 (STARTING)
            // -------------------------------------------------------
            if ( ProcessState.STARTING.getValue().equals(dbStatus)) {
                if (isPortUp) {
                    // 포트가 열렸다! -> RUNNING으로 변경
                    ps.setStatus(ProcessState.RUNNING.getValue());
                    ps.setStartTime(currentTime);
                    log.info("[{}] Start Complete. Changed to RUNNING.", ps.getProcessName());

                    processStatusHistory =
                            ProcessStatusHistory.builder()
                                    .eventTime(currentTime)
                                    .port(port)
                                    .pid(pid.intValue())
                                    .processName(ps.getProcessName())
                                    .status(ProcessState.RUNNING.getValue())
                                    .startTime(currentTime)
                                    .build();
                }
                // 아직 안 열렸으면? -> 그냥 둠 (다음 스케줄러가 또 확인)
                // 만약 START_REQUEST_TIME이 5분 지났는데도 안 켜지면 STOPPED로 바꾸는 타임아웃 로직 추가 가능
            }

            // -------------------------------------------------------
            // CASE B: 꺼지는 중 (STOPPING)
            // -------------------------------------------------------
            else if ( ProcessState.STOPPING.getValue().equals(dbStatus)) {
                if (!isPidAlive && !isPortUp) {
                    // PID도 없고 포트도 닫혔다! -> STOPPED로 변경
                    ps.setStatus( ProcessState.DOWN.getValue());
                    ps.setEndTime(currentTime);
                    ps.setPid(null); // PID 초기화
                    log.info("[{}] Stop Complete. Changed to STOPPED.", ps.getProcessName());
                    processStatusHistory =
                            ProcessStatusHistory.builder()
                                    .eventTime(currentTime)
                                    .port(port)
                                    .processName(ps.getProcessName())
                                    .status(ProcessState.DOWN.getValue())
                                    .endTime(currentTime)
                                    .build();
                }
            }

            // -------------------------------------------------------
            // CASE C: 잘 돌고 있어야 함 (RUNNING) -> 근데 죽었나? (Health Check)
            // -------------------------------------------------------
            else if (ProcessState.RUNNING.getValue().equals(dbStatus)) {
                if (!isPortUp) {
                    // 어? DB는 RUNNING인데 포트가 죽었네? (비정상 종료 감지)
                    log.error("[{}] Detected Abnormal Shutdown!", ps.getProcessName());
                    ps.setStatus(ProcessState.DOWN.getValue());
                    ps.setEndTime(currentTime);
                    ps.setPid(null);

                    processStatusHistory =
                            ProcessStatusHistory.builder()
                                    .eventTime(currentTime)
                                    .port(port)
                                    .processName(ps.getProcessName())
                                    .status(ProcessState.DOWN.getValue())
                                    .endTime(currentTime)
                                    .build();
                }
            }

            // -------------------------------------------------------
            // CASE D: 꺼져 있어야 함 (STOPPED) -> 근데 켜졌나? (외부에서 켰을 때)
            // -------------------------------------------------------
            else if (ProcessState.DOWN.getValue().equals(dbStatus)) {
                if (isPortUp) {
                    // 누가 몰래 켰다! (동기화)
                    ps.setStatus(ProcessState.RUNNING.getValue());
                    ps.setStartTime(currentTime);
                    if (processId != null) {
                        try {
                            ps.setPid(processIdByInteger);
                            processStatusHistory =
                                    ProcessStatusHistory.builder()
                                            .eventTime(currentTime)
                                            .port(port)
                                            .pid(processIdByInteger)
                                            .processName(ps.getProcessName())
                                            .status(ProcessState.DOWN.getValue())
                                            .startTime(currentTime)
                                            .build();
                        } catch (NumberFormatException e) {
                            log.warn("PID parsing failed for port {}", port);
                        }
                    }
                }
            }

            if(processStatusHistory != null){
                processStatusHistoryRepository.save(processStatusHistory);
            }
            processStatusRepository.save(ps);
        }
    }
}
