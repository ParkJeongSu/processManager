package kr.co.aim.infra.persistence.adapter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.aim.common.condition.ProcessStatusHistoryCondition;
import kr.co.aim.domain.model.ProcessStatusHistory;
import kr.co.aim.domain.repository.ProcessStatusHistoryRepository;
import kr.co.aim.infra.persistence.entity.ProcessStatusHistoryEntity;
import kr.co.aim.infra.persistence.mapper.ProcessStatusHistoryMapper;
import kr.co.aim.infra.persistence.springdatajpa.ProcessStatusHistoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kr.co.aim.infra.persistence.entity.QProcessStatusHistoryEntity.processStatusHistoryEntity;

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */


@Repository
@RequiredArgsConstructor
public class ProcessStatusHistoryRepositoryImpl implements ProcessStatusHistoryRepository {

    private final ProcessStatusHistoryJpaRepository processStatusHistoryJpaRepository;
    private final ProcessStatusHistoryMapper processStatusHistoryMapper;
    private final JPAQueryFactory queryFactory; // ✨ JPAQueryFactory 주입

    @Override
    public ProcessStatusHistory save(ProcessStatusHistory processStatus) {
        ProcessStatusHistoryEntity entity = processStatusHistoryMapper.toEntity(processStatus);
        ProcessStatusHistoryEntity savedEntity = processStatusHistoryJpaRepository.save(entity);
        return processStatusHistoryMapper.toDomain(savedEntity);
    }

    @Override
    public Page<ProcessStatusHistory> findProcessStatusHistoryWithConditions(ProcessStatusHistoryCondition condition, Pageable pageable) {
        JPAQuery<ProcessStatusHistoryEntity> query = queryFactory
                .select(processStatusHistoryEntity)
                .from(processStatusHistoryEntity)
                .where(                        // ** 동일한 WHERE 조건 **
                        portEq(condition.getPort()),
                        processNameContains(condition.getProcessName()),
                        eventTimeGoe(condition.getFromEventTime()),
                        eventTimeLoe(condition.getToEventTime())
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
        List<ProcessStatusHistory> content = query.fetch().stream().map(processStatusHistoryMapper::toDomain).collect(Collectors.toList());

        // 5. [수정] 카운트 조회 분기
        long total;
        if (pageable.isPaged()) {
            // [페이징 O] 별도 카운트 쿼리 실행
            Long count = queryFactory
                    .select(processStatusHistoryEntity.count())
                    .from(processStatusHistoryEntity)
                    .where(
                            // ** 동일한 WHERE 조건 **
                            portEq(condition.getPort()),
                            processNameContains(condition.getProcessName()),
                            eventTimeGoe(condition.getFromEventTime()),
                            eventTimeLoe(condition.getToEventTime())
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

    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();

        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                // 정렬 방향을 결정합니다 (ASC or DESC)
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                // 정렬할 속성(컬럼)을 PathBuilder를 통해 지정합니다.
                // "userName"과 같은 문자열을 Q-Type 경로로 변환해줍니다.
                PathBuilder pathBuilder = new PathBuilder<>(processStatusHistoryEntity.getType(), processStatusHistoryEntity.getMetadata());

                orders.add(new OrderSpecifier(direction, pathBuilder.get(order.getProperty())));
            }
        }

        // 기본 정렬 조건 (만약 정렬 조건이 없다면 id 내림차순)
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier(Order.ASC, processStatusHistoryEntity.eventTime));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }


    // == 동적 쿼리를 위한 BooleanExpression 메소드들 ==
    private BooleanExpression portEq(Integer port) {
        return port!=null ? processStatusHistoryEntity.port.eq(port) : null;
    }

    private BooleanExpression processNameContains(String processName) {
        return StringUtils.hasText(processName) ? processStatusHistoryEntity.processName.contains(processName) : null;
    }

    /**
     * [추가] 이벤트 시작 시간 조건 (eventTime >= fromEventTime)
     */
    private BooleanExpression eventTimeGoe(LocalDateTime fromEventTime) {
        if (fromEventTime == null) {
            return null;
        }
        return processStatusHistoryEntity.eventTime.goe(fromEventTime);
    }

    /**
     * [추가] 이벤트 종료 시간 조건 (eventTime <= toEventTime)
     */
    private BooleanExpression eventTimeLoe(LocalDateTime toEventTime) {
        if (toEventTime == null) {
            return null;
        }
        return processStatusHistoryEntity.eventTime.loe(toEventTime);
    }
}
