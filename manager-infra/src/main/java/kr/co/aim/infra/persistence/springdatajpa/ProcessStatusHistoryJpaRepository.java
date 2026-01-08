package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.ProcessStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessStatusHistoryJpaRepository extends JpaRepository<ProcessStatusHistoryEntity, Integer> {
}
