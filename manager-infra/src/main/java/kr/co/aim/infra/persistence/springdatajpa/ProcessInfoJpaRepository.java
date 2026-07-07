package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.ProcessInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProcessInfoJpaRepository extends JpaRepository<ProcessInfoEntity, Long> {
    List<ProcessInfoEntity> findBySystemName(String systemName);
    Optional<ProcessInfoEntity> findByPort(Integer portName);
}
