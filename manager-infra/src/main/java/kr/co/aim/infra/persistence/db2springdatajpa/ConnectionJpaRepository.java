package kr.co.aim.infra.persistence.db2springdatajpa;

import kr.co.aim.infra.persistence.db2entity.DualEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionJpaRepository extends JpaRepository<DualEntity, Long> {
}
