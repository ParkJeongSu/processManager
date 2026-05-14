package kr.co.aim.infra.persistence.adapter;

import kr.co.aim.domain.repository.DB2ConnectionRepository;
import kr.co.aim.domain.repository.MonitoringRepository;
import kr.co.aim.infra.persistence.db2springdatajpa.ConnectionJpaRepository;
import kr.co.aim.infra.persistence.springdatajpa.MonitoringJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */

@Repository
@RequiredArgsConstructor
public class DB2ConnectionRepositoryImpl implements DB2ConnectionRepository {

    private final ConnectionJpaRepository connectionJpaRepository;

    @Override
    public Integer checkConnection() {
        return connectionJpaRepository.checkConnection();
    }
}
