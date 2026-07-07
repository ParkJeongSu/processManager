package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.domain.model.ProcessStatus;
import kr.co.aim.infra.persistence.entity.ProcessStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessStatusJpaRepository extends JpaRepository<ProcessStatusEntity, Long> {
    Optional<ProcessStatusEntity> findByPort(Integer port);
}
