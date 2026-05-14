package kr.co.aim.api.service;

import kr.co.aim.common.enums.SystemName;
import kr.co.aim.common.vo.ProcessControlRequestVo;
import kr.co.aim.common.vo.ProcessStatusResponseVo;
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
public class ProcessService {
    private final ProcessInfoService processInfoService;
    private final ProcessStatusService processStatusService;
    private final ConnectionCheckService connectionCheckService;
    // HTTP 요청을 보내기 위한 도구 (Spring 기본 제공)
    private final RestTemplate restTemplate = new RestTemplate();

    public List<ProcessStatusResponseDto> getProcessList(){
        List<ProcessStatusResponseDto> resultList = new ArrayList<>();
        List<ProcessInfo> processes = processInfoService.findAll();
        List<ProcessStatus> processStatuses = processStatusService.findAll();
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


        // Gal
        boolean galStatus = connectionCheckService.getGalDbStatus();
        ProcessStatusResponseDto galDto = new ProcessStatusResponseDto();
        galDto.setSystemName("GAL");
        galDto.setProcessGroupName("GAL");
        galDto.setProcessName("GAL");
        galDto.setStatus(galStatus ? "UP" : "DOWN");
        resultList.add(galDto);

        // Manti
        boolean mantiStatus = connectionCheckService.getMantiStatus();
        ProcessStatusResponseDto mantiDto = new ProcessStatusResponseDto();
        mantiDto.setSystemName("MANTI");
        mantiDto.setProcessGroupName("MANTI");
        mantiDto.setProcessName("MANTI");
        mantiDto.setStatus(mantiStatus ? "UP" : "DOWN");
        resultList.add(mantiDto);

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
    public ProcessStatusResponseVo startProcess(int port, ProcessControlRequestVo vo) {

        Optional<ProcessInfo> optionalProcessInfo = processInfoService.findByPort(port);
        if(optionalProcessInfo.isEmpty()){
            throw new IllegalArgumentException("해당 포트(" + port + ")의 프로세스 설정 정보가 없습니다.");
        }
        ProcessInfo processInfo = optionalProcessInfo.get();

        // 1. 방어 로직
        if (isRunning(port)) {
            throw new IllegalStateException("이미 실행 중인 프로세스입니다. (Port: " + port + ")");
        }
        LocalDateTime currentTime = LocalDateTime.now();

        // 1.1 방어로직 스케줄러가 State를 바꾼후 시작할 수 있게끔
        Optional<ProcessStatus> optionalProcessStatus = processStatusService.findByPort(port);
        ProcessStatus processStatus = null;
        if(optionalProcessStatus.isPresent()){
            processStatus = optionalProcessStatus.get();
            if(processStatus.getStatus().equals(ProcessState.STARTING.getValue())){
                throw new IllegalStateException("이미 실행 중인 프로세스입니다. (Port: " + port + ")");
            }
            else if(processStatus.getStatus().equals(ProcessState.STOPPING.getValue())){
                throw new IllegalStateException("이미 종료 중인 프로세스입니다. (Port: " + port + ")");
            }
        }
        if(optionalProcessStatus.isEmpty()){
            processStatus = new ProcessStatus();
            processStatus.setPort(port);
            processStatus.setProcessName(processInfo.getProcessName());
        }
        processStatus.setStatus(ProcessState.STARTING.getValue());
        processStatus.setStartRequestTime(currentTime);
        // 재시작일 수 있으니 이전 종료 시간 등은 초기화하지 않음 (정책에 따라 결정)
        processStatusService.save(processStatus);

        ProcessStatusHistory processStatusHistory =
                ProcessStatusHistory.builder()
                        .eventTime(currentTime)
                        .port(port)
                        .processName(processInfo.getProcessName())
                        .status(ProcessState.STARTING.getValue())
                        .startRequestTime(currentTime)
                        .build();
        processStatusService.save(processStatusHistory);

        try {
            // 2-1. 파일 복사
            String copyDirStr = processInfo.getCopyDir();
            String workDirStr = processInfo.getWorkingDir();
            String fileName   = processInfo.getFileName();

            if( StringUtils.isBlank(copyDirStr)){
                // copyDirStr이 null 인 경우
                // 복사 수행 안함
            }else{
                Path sourcePath = Paths.get(copyDirStr, fileName);
                Path targetPath = Paths.get(workDirStr, fileName);
                File workingDirectory = new File(workDirStr);

                if (!Files.exists(sourcePath)) {
                    throw new IOException("원본 파일이 존재하지 않습니다: " + sourcePath);
                }

                if (!workingDirectory.exists()) {
                    workingDirectory.mkdirs();
                }

                if(StringUtils.isNotBlank(copyDirStr) || !StringUtils.equals(copyDirStr, workDirStr) ){
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    log.info("File Copied: {} -> {}", sourcePath, targetPath);
                }
            }

            // 2-2. 프로세스 실행
            List<String> commands = new ArrayList<>();
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("start");
            commands.add("/b");
            // 배치 파일의 절대 경로 생성
            String batchFullPath = processInfo.getBatchDir() + File.separator + processInfo.getBatchName();
            commands.add(batchFullPath);

            // [분기 처리] MNG 시스템인 경우에만 아규먼트 추가
            if (StringUtils.equals(SystemName.MNG.getValue(), processInfo.getSystemName())) {
                // 순서: 1. 작업디렉토리, 2. 파일명
                commands.add(processInfo.getWorkingDir());
                commands.add(processInfo.getFileName());
                log.info("MNG System detected. Arguments added: {} {}", processInfo.getWorkingDir(), processInfo.getFileName());
            } else {
                // 나머지 시스템은 아규먼트 없이 배치 파일만 실행
                log.info("General System detected. Executing batch without extra arguments.");
            }

            ProcessBuilder pb = new ProcessBuilder(commands);

            // [중요] 배치 파일이 있는 경로로 '이동'하여 실행하는 효과
            File batchDirectory = new File(processInfo.getBatchDir());
            if (!batchDirectory.exists()) {
                batchDirectory.mkdirs();
            }
            pb.directory(batchDirectory);

            // 로그를 저장할 파일 생성 (예: C:\mng\logs\process_start.log)
            File logFile = new File(processInfo.getBatchDir(), "batch_exec.log");

            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));
            // TODO: 추후 테스트가 완료되면 아래로 로그 삭제
            //pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            //pb.redirectError(ProcessBuilder.Redirect.DISCARD);

            pb.start();

            log.info("Process Started Command Sent: {}", processInfo.getProcessName());

            // 4. 결과 Vo 반환
            return ProcessStatusResponseVo.builder()
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
    public ProcessStatusResponseVo stopProcess(int port, ProcessControlRequestVo requestVo) {

        if (!isRunning(port)) {
            throw new IllegalStateException("이미 종료된 프로세스이거나 연결할 수 없습니다.");
        }

        Optional<ProcessInfo> optionalProcessInfo = processInfoService.findByPort(port);

        if(optionalProcessInfo.isEmpty()){
            throw new IllegalArgumentException("설정 정보 없음");
        }
        ProcessInfo processInfo = optionalProcessInfo.get();

        // 현재 stopping 하고 있는 명령어라면 종료
        processStatusService.checkStoppingStatus(port);

        try {
            processStatusService.markAsStopping(port, processInfo, requestVo.getEventUser());
        } catch (Exception e) {
            log.error("상태 업데이트(STOPPING) 실패 했으나 프로세스 종료는 계속 진행함", e);
            // DB 업데이트 실패해도 실제 프로세스 종료는 시도할지 여부는 정책에 따라 결정
        }


        // url : graceful shutdown url
        String url = "http://localhost:" + port + "/wcs-web" + "/stop";
        log.info("종료 요청 전송: {}", url);

        try {
            // 1. 종료 요청
            String reply = restTemplate.postForObject(url, null, String.class);
            LocalDateTime currentTime = LocalDateTime.now();

            processStatusService.markAsDown(port,requestVo);

            log.info("Process Stop Command Sent: {}", processInfo.getProcessName());

            return ProcessStatusResponseVo.builder()
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

        List<ProcessStatus> processList = processStatusService.findAll();

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

            if(ObjectUtils.isNotEmpty(processStatusHistory)){
                processStatusService.save(processStatusHistory);
            }
            processStatusService.save(ps);
        }
    }
}
