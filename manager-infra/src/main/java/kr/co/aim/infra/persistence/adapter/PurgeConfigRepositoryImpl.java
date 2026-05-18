package kr.co.aim.infra.persistence.adapter;

import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.repository.PurgeConfigRepository;
import kr.co.aim.infra.persistence.entity.PurgeConfigEntity;
import kr.co.aim.infra.persistence.mapper.PurgeConfigMapper;
import kr.co.aim.infra.persistence.springdatajpa.PurgeConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PurgeConfigRepositoryImpl implements PurgeConfigRepository {

    private final PurgeConfigJpaRepository purgeConfigJpaRepository;
    private final PurgeConfigMapper purgeConfigMapper;


    @Override
    public List<PurgeConfig> findAll() {
        return purgeConfigJpaRepository.findAll().stream().map(purgeConfigMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<PurgeConfig> findById(Integer id) {
        return purgeConfigJpaRepository.findById(id).map(purgeConfigMapper::toDomain);
    }

    @Override
    public PurgeConfig save(PurgeConfig purgeConfig) {
        PurgeConfigEntity entity = purgeConfigMapper.toEntity(purgeConfig);
        PurgeConfigEntity savedEntity = purgeConfigJpaRepository.save(entity);
        return purgeConfigMapper.toDomain(savedEntity);
    }

    @Override
    public void deleteAllByIdInBatch(List<Integer> ids) {
        purgeConfigJpaRepository.deleteAllByIdInBatch(ids);
    }
}
