package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.ProcessInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessInfoJpaRepository extends JpaRepository<ProcessInfoEntity, Integer> {
    List<ProcessInfoEntity> findBySystemName(String systemName);
}
