package kr.co.aim.infra.persistence.adapter;

import kr.co.aim.domain.repository.MonitoringRepository;
import kr.co.aim.infra.persistence.springdatajpa.MonitoringJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */

@Repository
@RequiredArgsConstructor
public class MonitoringRepositoryImpl implements MonitoringRepository {

    private final MonitoringJpaRepository monitoringJpaRepository;

    @Override
    public Long getSessionCount() {
        return monitoringJpaRepository.getSessionCount();
    }

    @Override
    public Long getLockCount() {
        return monitoringJpaRepository.getLockCount();
    }

    @Override
    public Integer getCpuUsage() {
        return monitoringJpaRepository.getCpuUsage();
    }

    @Override
    public Double getLogUsagePercentage() {
        return monitoringJpaRepository.getLogUsagePercentage();
    }

    @Override
    public Double getTempDbFreePercentage() {
        return monitoringJpaRepository.getTempDbFreePercentage();
    }

    @Override
    public Integer getMaxQueryTime() {
        return monitoringJpaRepository.getMaxQueryTime();
    }

    @Override
    public Integer checkConnection() {
        return monitoringJpaRepository.checkConnection();
    }
}
