package kr.co.aim.infra.persistence.adapter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.aim.common.vo.ProcessInfoSearchConditionVo;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.domain.repository.PurgeConfigRepository;
import kr.co.aim.infra.persistence.entity.ProcessInfoEntity;
import kr.co.aim.infra.persistence.entity.PurgeConfigEntity;
import kr.co.aim.infra.persistence.mapper.ProcessInfoMapper;
import kr.co.aim.infra.persistence.mapper.PurgeConfigMapper;
import kr.co.aim.infra.persistence.springdatajpa.ProcessInfoJpaRepository;
import kr.co.aim.infra.persistence.springdatajpa.PurgeConfigJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static kr.co.aim.infra.persistence.entity.QProcessInfoEntity.processInfoEntity;

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
    public PurgeConfig save(PurgeConfig purgeConfig) {
        PurgeConfigEntity entity = purgeConfigMapper.toEntity(purgeConfig);
        PurgeConfigEntity savedEntity = purgeConfigJpaRepository.save(entity);
        return purgeConfigMapper.toDomain(savedEntity);
    }
}
