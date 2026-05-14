package kr.co.aim.infra.persistence.adapter;

import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.model.PurgeLog;
import kr.co.aim.domain.repository.PurgeConfigRepository;
import kr.co.aim.domain.repository.PurgeLogRepository;
import kr.co.aim.infra.persistence.entity.PurgeLogEntity;
import kr.co.aim.infra.persistence.mapper.PurgeConfigMapper;
import kr.co.aim.infra.persistence.mapper.PurgeLogMapper;
import kr.co.aim.infra.persistence.springdatajpa.PurgeConfigJpaRepository;
import kr.co.aim.infra.persistence.springdatajpa.PurgeLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.engine.internal.Collections;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PurgeLogRepositoryImpl implements PurgeLogRepository {

    private final PurgeLogJpaRepository purgeLogJpaRepository;
    private final PurgeLogMapper purgeLogMapper;

    @Override
    public List<PurgeLog> findAll() {
        return purgeLogJpaRepository.findAll().stream().map(purgeLogMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public PurgeLog save(PurgeLog purgeLog) {
        PurgeLogEntity entity = purgeLogMapper.toEntity(purgeLog);
        PurgeLogEntity savedEntity = purgeLogJpaRepository.save(entity);
        return purgeLogMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<PurgeLog> findByBatchIdAndTableName(String batchId, String tableName) {
        return purgeLogJpaRepository.findByBatchIdAndTableName(batchId, tableName).map(purgeLogMapper::toDomain);
    }
}
