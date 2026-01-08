package kr.co.aim.api.service;

import kr.co.aim.api.jwt.JwtTokenProvider;
import kr.co.aim.common.dto.*;
import kr.co.aim.common.enums.EventName;
import kr.co.aim.common.error.ExcelValidationException;
import kr.co.aim.common.record.TransactionInfo;
import kr.co.aim.domain.command.ProcessInfoCreateCommand;
import kr.co.aim.domain.command.ProcessInfoUpdateCommand;
import kr.co.aim.domain.command.UserCreateCommand;
import kr.co.aim.domain.command.UserUpdateCommand;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.model.User;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.domain.repository.UserRepository;
import kr.co.aim.infra.persistence.mapper.ProcessInfoMapper;
import kr.co.aim.infra.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다. (DI)
public class ProcessInfoService {

    private final ProcessInfoRepository processInfoRepository; // 구현체(Infra)가 아닌 인터페이스(Domain)에 의존
    private final ProcessInfoMapper processInfoMapper;

    @Transactional(readOnly = true)
    public Page<ProcessInfoResponseDto> findProcessInfoList(ProcessInfoSearchConditionDto condition,Pageable pageable) {
        //1. Repository에서 Page<Entity>를 조회합니다.

        Page<ProcessInfoResponseDto> page = processInfoRepository.findProcessInfoWithConditions(condition,pageable);

        return page;
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

    @Transactional // 이 메소드가 하나의 트랜잭션으로 동작하도록 보장합니다.
    public ProcessInfo changeProcessInfo(Integer port, ProcessInfoUpdateRequestDto requestDto) {
        // 1. Repository를 통해 Domain 객체를 가져온다.
        ProcessInfo processInfo;
        Optional<ProcessInfo> optionalProcessInfo = processInfoRepository.findByPort(port);
        if(optionalProcessInfo.isPresent()){
            processInfo = optionalProcessInfo.get();
        }
        else {
            throw new IllegalArgumentException("존재하지 않는 ProcessInfo입니다. ID: " + requestDto.getPort());
        }
        ProcessInfoUpdateCommand command =
                ProcessInfoUpdateCommand.builder()
                        .port(port)
                        .systemName(requestDto.getSystemName())
                        .processGroupName(requestDto.getProcessGroupName())
                        .processName(requestDto.getProcessName())
                        .description(requestDto.getDescription())
                        .copyDir(requestDto.getCopyDir())
                        .workingDir(requestDto.getWorkingDir())
                        .fileName(requestDto.getFileName())
                        .command(requestDto.getCommand())
                        .build();
        processInfo.changeProcessInfo(command);

        return processInfoRepository.save(processInfo);
    }

    @Transactional // 이 메소드가 하나의 트랜잭션으로 동작하도록 보장합니다.
    public ProcessInfo createProcessInfo(ProcessInfoCreateRequestDto requestDto) {
        // 1. Repository를 통해 Domain 객체를 가져온다.

        Optional<ProcessInfo> optionalProcessInfo = processInfoRepository.findByPort(requestDto.getPort());
        if(optionalProcessInfo.isPresent()){
            throw new IllegalArgumentException("이미 생성된 ProcessInfo입니다. ID: " + requestDto.getPort());
        }

        ProcessInfoCreateCommand command =
                ProcessInfoCreateCommand.builder()
                        .port(requestDto.getPort())
                        .systemName(requestDto.getSystemName())
                        .processGroupName(requestDto.getProcessGroupName())
                        .processName(requestDto.getProcessName())
                        .description(requestDto.getDescription())
                        .copyDir(requestDto.getCopyDir())
                        .workingDir(requestDto.getWorkingDir())
                        .fileName(requestDto.getFileName())
                        .command(requestDto.getCommand())
                        .build();

        ProcessInfo processInfo = ProcessInfo.create(command);
        return processInfoRepository.save(processInfo);
    }

    @Transactional
    public void createProcessInfo(List<ProcessInfoCreateRequestDto> requestDtoList) {

        if (requestDtoList == null || requestDtoList.isEmpty()) {
            return;
        }

        List<String> errorMessages = new ArrayList<>();
        for(ProcessInfoCreateRequestDto processInfoDto : requestDtoList ){
            Optional<ProcessInfo> optionalProcessInfo = processInfoRepository.findByPort(processInfoDto.getPort());
            if(optionalProcessInfo.isPresent()){
                errorMessages.add("이미 생성된 ProcessInfo입니다. port: " + processInfoDto.getPort());
            }
            if(!StringUtils.hasText(processInfoDto.getProcessName())) {
                errorMessages.add("ProcessName 가 비어있습니다.");
            }
        }

        if (!errorMessages.isEmpty()) {
            throw new ExcelValidationException(errorMessages);
        }

        List<ProcessInfo> processInfoToSave = new ArrayList<>();
        for(ProcessInfoCreateRequestDto requestDto : requestDtoList ){
            ProcessInfoCreateCommand command =
                    ProcessInfoCreateCommand.builder()
                            .port(requestDto.getPort())
                            .systemName(requestDto.getSystemName())
                            .processGroupName(requestDto.getProcessGroupName())
                            .processName(requestDto.getProcessName())
                            .description(requestDto.getDescription())
                            .copyDir(requestDto.getCopyDir())
                            .workingDir(requestDto.getWorkingDir())
                            .fileName(requestDto.getFileName())
                            .command(requestDto.getCommand())
                            .build();
            ProcessInfo processInfo = ProcessInfo.create(command);
            processInfoToSave.add(processInfo);
        }
        processInfoRepository.saveAll(processInfoToSave);
    }


}