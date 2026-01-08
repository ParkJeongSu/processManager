package kr.co.aim.infra.persistence.adapter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.aim.common.dto.*;
import kr.co.aim.domain.model.ProcessInfo;
import kr.co.aim.domain.repository.ProcessInfoRepository;
import kr.co.aim.infra.persistence.entity.ProcessInfoEntity;
import kr.co.aim.infra.persistence.entity.UserEntity;
import kr.co.aim.infra.persistence.mapper.ProcessInfoMapper;
import kr.co.aim.infra.persistence.springdatajpa.ProcessInfoJpaRepository;
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

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */

@Repository
@RequiredArgsConstructor
public class ProcessInfoRepositoryImpl implements ProcessInfoRepository {

    private final ProcessInfoJpaRepository processInfoJpaRepository;
    private final ProcessInfoMapper processInfoMapper;
    private final JPAQueryFactory queryFactory; // ✨ JPAQueryFactory 주입

    @Override
    public ProcessInfo save(ProcessInfo processInfo) {
        ProcessInfoEntity entity = processInfoMapper.toEntity(processInfo);
        ProcessInfoEntity savedEntity = processInfoJpaRepository.save(entity);
        return processInfoMapper.toDomain(savedEntity);
    }

    @Override
    public List<ProcessInfo> findAll() {
        return processInfoJpaRepository.findAll().stream().map(processInfoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<ProcessInfo> findByPort(Integer port) {
        return processInfoJpaRepository.findById(port).map(processInfoMapper::toDomain);
    }

    @Override
    public List<ProcessInfo> findBySystemName(String systemName) {
        return processInfoJpaRepository.findBySystemName(systemName).stream().map(processInfoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteAllByIdInBatch(List<Integer> ids) {
        processInfoJpaRepository.deleteAllByIdInBatch(ids);
    }

    @Override
    public Page<ProcessInfoResponseDto> findProcessInfoWithConditions(ProcessInfoSearchConditionDto condition, Pageable pageable) {
        // 1. 공통 쿼리 빌더 생성 (SELECT, FROM, JOIN, WHERE)
        JPAQuery<ProcessInfoResponseDto> query = queryFactory
                .select(new QProcessInfoResponseDto(
                            processInfoEntity.port,
                            processInfoEntity.systemName,
                            processInfoEntity.processGroupName,
                            processInfoEntity.processName,
                            processInfoEntity.description,
                            processInfoEntity.copyDir,
                            processInfoEntity.workingDir,
                            processInfoEntity.fileName,
                            processInfoEntity.command
                ))
                .from(processInfoEntity)
                .where(
                        // ** 동일한 WHERE 조건 **
                        portEq(condition.getPort()),
                        systemNameContains(condition.getSystemName()),
                        processGroupNameContains(condition.getProcessGroupName()),
                        processNameContains(condition.getProcessName())
                );

        // 2. [수정] 정렬은 페이징과 상관없이 공통 적용
        query.orderBy(getOrderSpecifiers(pageable.getSort()));

        // 3. [수정] 페이징 적용 분기
        if (pageable.isPaged()) {
            // .unpaged()가 아닐 때만 offset/limit 적용
            query.offset(pageable.getOffset());
            query.limit(pageable.getPageSize());
        }

        // 4. 데이터 조회
        List<ProcessInfoResponseDto> content = query.fetch();

        // 5. [수정] 카운트 조회 분기
        long total;
        if (pageable.isPaged()) {
            // [페이징 O] 별도 카운트 쿼리 실행
            Long count = queryFactory
                    .select(processInfoEntity.count())
                    .from(processInfoEntity)
                    .where(
                            // ** 동일한 WHERE 조건 **
                            portEq(condition.getPort()),
                            systemNameContains(condition.getSystemName()),
                            processGroupNameContains(condition.getProcessGroupName()),
                            processNameContains(condition.getProcessName())
                    )
                    .fetchOne();

            // fetchOne()은 결과가 없으면 null을 반환할 수 있으므로 null 체크
            total = (count != null) ? count.longValue() : 0L;

        } else {
            // [페이징 X] .unpaged() 일 때
            // 이미 모든 데이터를 가져왔으므로 content.size()가 total
            total = content.size();
        }

        // 6. PageImpl 반환
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public List<ProcessInfo> saveAll(List<ProcessInfo> processInfoList) {
        List<ProcessInfoEntity> entityList = processInfoList.stream().map(processInfoMapper::toEntity).collect(Collectors.toList());
        List<ProcessInfoEntity> savedEntityList = processInfoJpaRepository.saveAll(entityList);
        return savedEntityList.stream().map(processInfoMapper::toDomain).collect(Collectors.toList());
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();

        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                // 정렬 방향을 결정합니다 (ASC or DESC)
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                // 정렬할 속성(컬럼)을 PathBuilder를 통해 지정합니다.
                // "userName"과 같은 문자열을 Q-Type 경로로 변환해줍니다.
                PathBuilder pathBuilder = new PathBuilder<>(processInfoEntity.getType(), processInfoEntity.getMetadata());

                orders.add(new OrderSpecifier(direction, pathBuilder.get(order.getProperty())));
            }
        }

        // 기본 정렬 조건 (만약 정렬 조건이 없다면 id 내림차순)
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier(Order.ASC, processInfoEntity.port));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }


    // == 동적 쿼리를 위한 BooleanExpression 메소드들 ==
    private BooleanExpression portEq(Integer port) {
        return port!=null ? processInfoEntity.port.eq(port) : null;
    }

    private BooleanExpression systemNameContains(String systemName) {
        return StringUtils.hasText(systemName) ? processInfoEntity.systemName.contains(systemName) : null;
    }

    private BooleanExpression processGroupNameContains(String processGroupName) {
        return StringUtils.hasText(processGroupName) ? processInfoEntity.processGroupName.contains(processGroupName) : null;
    }

    private BooleanExpression processNameContains(String processName) {
        return StringUtils.hasText(processName) ? processInfoEntity.processName.contains(processName) : null;
    }

}
