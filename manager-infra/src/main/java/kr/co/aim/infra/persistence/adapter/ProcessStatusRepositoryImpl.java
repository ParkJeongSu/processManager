package kr.co.aim.infra.persistence.adapter;

import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.domain.repository.ProcessStatusRepository;
import kr.co.aim.infra.persistence.entity.ProcessStatusEntity;
import kr.co.aim.infra.persistence.mapper.ProcessInfoMapper;
import kr.co.aim.infra.persistence.mapper.ProcessStatusMapper;
import kr.co.aim.infra.persistence.springdatajpa.ProcessInfoJpaRepository;
import kr.co.aim.infra.persistence.springdatajpa.ProcessStatusJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */

@Repository
@RequiredArgsConstructor
public class ProcessStatusRepositoryImpl implements ProcessStatusRepository {

    private final ProcessStatusJpaRepository processStatusJpaRepository;
    private final ProcessStatusMapper processStatusMapper;


    @Override
    public List<ProcessStatus> findAll() {
        return processStatusJpaRepository.findAll().stream().map(processStatusMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<ProcessStatus> findByPort(Integer port) {
        return processStatusJpaRepository.findById(port).map(processStatusMapper::toDomain);
    }

    @Override
    public ProcessStatus save(ProcessStatus processStatus) {
        ProcessStatusEntity entity = processStatusMapper.toEntity(processStatus);
        ProcessStatusEntity savedEntity = processStatusJpaRepository.save(entity);
        return processStatusMapper.toDomain(savedEntity);
    }
}
