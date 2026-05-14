package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.PurgeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PurgeLogJpaRepository extends JpaRepository<PurgeLogEntity, Long> {
    Optional<PurgeLogEntity> findByBatchIdAndTableName(String batchId, String tableName);
}
