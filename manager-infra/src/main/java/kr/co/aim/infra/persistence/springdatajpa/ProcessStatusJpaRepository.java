package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.ProcessStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessStatusJpaRepository extends JpaRepository<ProcessStatusEntity, Integer> {
}
