package kr.co.aim.api.service;

import kr.co.aim.common.enums.ProcessState;
import kr.co.aim.common.condition.ProcessControlRequestCondition;
import kr.co.aim.common.condition.ProcessStatusHistoryCondition;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.domain.model.ProcessStatusHistory;
import kr.co.aim.domain.repository.ProcessStatusHistoryRepository;
import kr.co.aim.domain.repository.ProcessStatusRepository;
import kr.co.aim.infra.persistence.mapper.ProcessStatusMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (DI)
public class ProcessStatusService {

    private final ProcessStatusRepository processStatusRepository;
    private final ProcessStatusHistoryRepository processStatusHistoryRepository;
    private final ProcessStatusMapper processStatusMapper;
    private final ProcessInfoService processInfoService;
    private final ConnectionCheckService connectionCheckService;

    @Transactional
    public List<ProcessStatus> findAll() {
        return processStatusRepository.findAll();
    }


    public List<ProcessStatus> findAllProcess()
    {
        List<ProcessStatus> processStatuseList = processStatusRepository.findAll();
        boolean galStatus = connectionCheckService.getGalDbStatus();
        ProcessStatus galProcessStatus =
                ProcessStatus
                        .builder()
                        //.port()
                        .processName("GAL")
                        .status(galStatus ? "UP" : "DOWN"   )
                        //.pid()
                        //.startRequestTime()
                        //.startTime()
                        //.endRequestTime()
                        //.endTime()
                        .build();
        processStatuseList.add(galProcessStatus);

        boolean mantiStatus = connectionCheckService.getMantiStatus();
        ProcessStatus mantiProcessStatus =
                ProcessStatus
                        .builder()
                        //.port()
                        .processName("MANTI")
                        .status(mantiStatus ? "UP" : "DOWN"   )
                        //.pid()
                        //.startRequestTime()
                        //.startTime()
                        //.endRequestTime()
                        //.endTime()
                        .build();
        processStatuseList.add(mantiProcessStatus);

        return processStatuseList;
    }

    @Transactional
    public Optional<ProcessStatus> findByPort(Integer port) {
        return processStatusRepository.findByPort(port);
    }

    @Transactional
    public ProcessStatus save(ProcessStatus processStatus) {
        return processStatusRepository.save(processStatus);
    }

    @Transactional
    public ProcessStatusHistory save(ProcessStatusHistory processStatusHistory) {
        return processStatusHistoryRepository.save(processStatusHistory);
    }

    @Transactional(readOnly = true)
    public Page<ProcessStatusHistory> findProcessStatusHistoryWithConditions(ProcessStatusHistoryCondition condition, Pageable pageable){
        return processStatusHistoryRepository.findProcessStatusHistoryWithConditions(condition, pageable);
    }

    @Transactional
    public void checkStoppingStatus(int port) {
        // 1. ProcessStatus 조회 및 업데이트
        Optional<ProcessStatus> optionalProcessStatus = this.findByPort(port);
        ProcessStatus ps = null;
        if(optionalProcessStatus.isPresent()){
            ps = optionalProcessStatus.get();
            if(StringUtils.equals(ProcessState.STOPPING.getValue(),ps.getStatus())){
                throw new RuntimeException("already Stopping");
            }
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
        Optional<ProcessStatus> optionalProcessStatus = this.findByPort(port);
        ProcessStatus ps = null;
        if(optionalProcessStatus.isPresent()){
            ps = optionalProcessStatus.get();
        }
        if (optionalProcessStatus.isEmpty()){
            ps = ProcessStatus
                    .builder()
                    .port(port)
                    .processName(processInfo.getProcessName())
                    .build();
        }
        // STOPPING 상태로 변경 (ProcessState Enum에 STOPPING이 있다고 가정)
        ps.setStatus(ProcessState.STOPPING.getValue());
        ps.setEndRequestTime(currentTime); // 수정 시간 업데이트
        this.save(ps);

        // 2. History 추가
        ProcessStatusHistory history = ProcessStatusHistory.builder()
                .eventTime(currentTime)
                .port(port)
                .processName(processInfo.getProcessName())
                .status(ProcessState.STOPPING.getValue()) // 히스토리에도 STOPPING 기록
                .endRequestTime(currentTime)
                .build();

        this.save(history);
    }

    /**
     * 프로세스 상태를 '종료 중(STOPPING)'으로 변경하고 히스토리를 남김
     * 독립적인 트랜잭션으로 즉시 커밋됨 (Propagation.REQUIRES_NEW)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsDown(int port, ProcessControlRequestCondition vo) {

        String userId = vo.getUserId();
        String eventName = vo.getEventName();
        LocalDateTime eventTime = vo.getEventTime();
        String eventUser = vo.getEventUser();
        String eventComment = vo.getEventComment();

        LocalDateTime currentTime = LocalDateTime.now();

        String processName = "";
        Optional<ProcessInfo> optionalProcessInfo = processInfoService.findByPort(port);
        if(optionalProcessInfo.isPresent()){
            ProcessInfo processInfo = optionalProcessInfo.get();
            processName = processInfo.getProcessName();
        }

        // 1. ProcessStatus 조회 및 업데이트
        Optional<ProcessStatus> optionalProcessStatus = this.findByPort(port);
        ProcessStatus ps = null;
        if(optionalProcessStatus.isPresent()){
            ps = optionalProcessStatus.get();
        }
        if (optionalProcessStatus.isEmpty()){
            ps = ProcessStatus
                    .builder()
                    .port(port)
                    .processName(processName)
                    .build();
        }
        // DOWN 상태로 변경
        ps.setStatus(ProcessState.DOWN.getValue());
        ps.setEndRequestTime(currentTime); // 수정 시간 업데이트
        this.save(ps);

        // 2. History 추가
        ProcessStatusHistory history = ProcessStatusHistory.builder()
                .eventTime(currentTime)
                .port(port)
                .processName(processName)
                .status(ProcessState.DOWN.getValue()) // 히스토리에도 DOWN 기록
                .endRequestTime(currentTime)
                .build();

        this.save(history);
    }


}