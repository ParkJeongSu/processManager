package kr.co.aim.infra.persistence.adapter;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.aim.domain.model.ProcessStatusHistory;
import kr.co.aim.domain.repository.ProcessStatusHistoryRepository;
import kr.co.aim.infra.persistence.entity.ProcessStatusHistoryEntity;
import kr.co.aim.infra.persistence.mapper.ProcessStatusHistoryMapper;
import kr.co.aim.infra.persistence.springdatajpa.ProcessStatusHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */


@Repository
@RequiredArgsConstructor
public class ProcessStatusHistoryRepositoryImpl implements ProcessStatusHistoryRepository {

    private final ProcessStatusHistoryJpaRepository processStatusHistoryJpaRepository;
    private final ProcessStatusHistoryMapper processStatusHistoryMapper;
    private final JPAQueryFactory queryFactory; // ✨ JPAQueryFactory 주입

    @Override
    public ProcessStatusHistory save(ProcessStatusHistory processStatus) {
        ProcessStatusHistoryEntity entity = processStatusHistoryMapper.toEntity(processStatus);
        ProcessStatusHistoryEntity savedEntity = processStatusHistoryJpaRepository.save(entity);
        return processStatusHistoryMapper.toDomain(savedEntity);
    }
}
