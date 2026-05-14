package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.PurgeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurgeConfigJpaRepository extends JpaRepository<PurgeConfigEntity, Integer> {
}
