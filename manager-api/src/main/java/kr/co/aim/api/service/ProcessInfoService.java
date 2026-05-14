package kr.co.aim.api.service;

import kr.co.aim.api.dto.ProcessInfoUpdateRequestDto;
import kr.co.aim.common.vo.ProcessInfoCreateRequestVo;
import kr.co.aim.common.vo.ProcessInfoSearchConditionVo;
import kr.co.aim.common.vo.ProcessInfoUpdateRequestVo;
import kr.co.aim.domain.command.ProcessInfoCreateCommand;
import kr.co.aim.domain.command.ProcessInfoUpdateCommand;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.infra.persistence.mapper.ProcessInfoMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (DI)
public class ProcessInfoService {

    private final ProcessInfoRepository processInfoRepository; // 구현체(Infra)가 아닌 인터페이스(Domain)에 의존
    private final ProcessInfoMapper processInfoMapper;

    @Transactional // 이 메소드가 하나의 트랜잭션으로 동작하도록 보장합니다.
    public ProcessInfo createProcessInfo(ProcessInfoCreateRequestVo vo) {
        // 1. Repository를 통해 Domain 객체를 가져온다.

        Optional<ProcessInfo> optionalProcessInfo = processInfoRepository.findByPort(vo.getPort());
        if(optionalProcessInfo.isPresent()){
            throw new IllegalArgumentException("이미 생성된 ProcessInfo입니다. ID: " + vo.getPort());
        }

        ProcessInfoCreateCommand command =
                ProcessInfoCreateCommand.builder()
                        .port(vo.getPort())
                        .systemName(vo.getSystemName())
                        .processGroupName(vo.getProcessGroupName())
                        .processName(vo.getProcessName())
                        .description(vo.getDescription())
                        .copyDir(vo.getCopyDir())
                        .workingDir(vo.getWorkingDir())
                        .fileName(vo.getFileName())
                        .batchDir(vo.getBatchDir())
                        .batchName(vo.getBatchName())
                        .build();

        ProcessInfo processInfo = ProcessInfo.create(command);
        return processInfoRepository.save(processInfo);
    }

    @Transactional
    public void createProcessInfo(List<ProcessInfoCreateRequestVo> voList) {
        if(CollectionUtils.isNotEmpty(voList)){
            for(ProcessInfoCreateRequestVo vo : voList){
                createProcessInfo(vo);
            }
        }
    }

    @Transactional
    public ProcessInfo changeProcessInfo(ProcessInfoUpdateRequestVo vo) {
        Integer port = vo.getPort();
        String systemName = vo.getSystemName();
        String processGroupName = vo.getProcessGroupName();
        String processName = vo.getProcessName();
        String description = vo.getDescription();
        String copyDir = vo.getCopyDir();
        String workingDir = vo.getWorkingDir();
        String fileName = vo.getFileName();
        String batchDir = vo.getBatchDir();
        String batchName = vo.getBatchName();
        ProcessInfo processInfo;
        Optional<ProcessInfo> optionalProcessInfo = findByPort(port);
        if(optionalProcessInfo.isPresent()){
            processInfo = optionalProcessInfo.get();
        }
        else {
            throw new IllegalArgumentException("존재하지 않는 ProcessInfo입니다. ID: " + port);
        }
        ProcessInfoUpdateCommand command =
                ProcessInfoUpdateCommand.builder()
                        .port(port)
                        .systemName(systemName)
                        .processGroupName(processGroupName)
                        .processName(processName)
                        .description(description)
                        .copyDir(copyDir)
                        .workingDir(workingDir)
                        .fileName(fileName)
                        .batchDir(batchDir)
                        .batchName(batchName)
                        .build();
        processInfo.changeProcessInfo(command);

        return processInfoRepository.save(processInfo);

    }

    @Transactional
    public void deleteProcessInfoByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return; // 삭제할 ID가 없으면 아무 작업도 하지 않음
        }
        // 여러 건을 삭제할 때는 이 메서드가 성능상 가장 효율적입니다.
        // DELETE ... WHERE id IN (...) 쿼리를 한 번에 실행합니다.
        processInfoRepository.deleteAllByIdInBatch(ids);
    }

    @Transactional(readOnly = true)
    public Page<ProcessInfo> findProcessInfoList(ProcessInfoSearchConditionVo condition, Pageable pageable) {
        //1. Repository에서 Page<Entity>를 조회합니다.

        Page<ProcessInfo> page = processInfoRepository.findProcessInfoWithConditions(condition,pageable);

        return page;
    }

    @Transactional
    public List<ProcessInfo> findBySystemName(String systemName) {
        return processInfoRepository.findBySystemName(systemName);
    }

    @Transactional
    public List<ProcessInfo> findAll() {
        return processInfoRepository.findAll();
    }

    @Transactional
    public Optional<ProcessInfo> findByPort(Integer port) {
        return processInfoRepository.findByPort(port);
    }


}