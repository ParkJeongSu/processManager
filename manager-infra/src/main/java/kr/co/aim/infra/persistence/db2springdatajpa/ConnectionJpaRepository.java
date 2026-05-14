package kr.co.aim.infra.persistence.db2springdatajpa;

import kr.co.aim.infra.persistence.db2entity.DualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConnectionJpaRepository extends JpaRepository<DualEntity, Long> {
    @Query(value = "SELECT 1 FROM SYSIBM.SYSDUMMY1", nativeQuery = true)
    Integer checkConnection();
}
