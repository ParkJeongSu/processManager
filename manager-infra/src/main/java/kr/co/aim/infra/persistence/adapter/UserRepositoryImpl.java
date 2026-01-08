package kr.co.aim.infra.persistence.adapter;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.aim.common.dto.QUserResponseDto;
import kr.co.aim.common.dto.UserResponseDto;
import kr.co.aim.common.dto.UserSearchConditionDto;
import kr.co.aim.domain.model.User;
import kr.co.aim.domain.repository.UserRepository;
import kr.co.aim.infra.persistence.entity.UserEntity;
import kr.co.aim.infra.persistence.mapper.UserMapper;
import kr.co.aim.infra.persistence.springdatajpa.UserJpaRepository;
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

import static kr.co.aim.infra.persistence.entity.QUserEntity.userEntity;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    // Spring Data JPA가 자동으로 구현해주는 JPA 리포지토리. UserEntity를 다룬다.
    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;
    private final JPAQueryFactory queryFactory; // ✨ JPAQueryFactory 주입

    @Override
    public User save(User user) {
        // 1. Domain -> Entity 변환
        UserEntity entity = userMapper.toEntity(user);
        // 2. JPA 리포지토리를 통해 DB에 저장
        UserEntity savedEntity = userJpaRepository.save(entity);
        // 3. 저장된 Entity -> Domain 변환 후 반환
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        // 1. JPA 리포지토리를 통해 ID로 Entity 조회
        Optional<UserEntity> entityOptional = userJpaRepository.findById(id);
        // 2. 조회된 Optional<Entity>를 Optional<Domain>으로 변환하여 반환
        return entityOptional.map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        // 1. JPA 리포지토리를 통해 Email로 Entity 조회
        Optional<UserEntity> entityOptional = userJpaRepository.findByEmail(email);
        // 2. 조회된 Optional<Entity>를 Optional<Domain>으로 변환하여 반환
        return entityOptional.map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        // 1. JPA 리포지토리를 통해 모든 UserEntity 조회
        List<UserEntity> entities = userJpaRepository.findAll();
        // 2. Entity 리스트를 Domain 객체 리스트로 변환하여 반환
        return entities.stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findByUserId(String userId) {
        // 1. JPA 리포지토리를 통해 Email로 Entity 조회
        Optional<UserEntity> entityOptional = userJpaRepository.findByUserId(userId);
        // 2. 조회된 Optional<Entity>를 Optional<Domain>으로 변환하여 반환
        return entityOptional.map(userMapper::toDomain);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        Page<UserEntity> userEntityPage = userJpaRepository.findAll(pageable);
        return userEntityPage.map(userMapper::toDomain);
    }

    @Override
    public void deleteAllByIdInBatch(List<Long> ids) {
        userJpaRepository.deleteAllByIdInBatch(ids);
    }


    @Override
    public Page<UserResponseDto> findUsersWithConditions(UserSearchConditionDto condition, Pageable pageable) {
        // 1. 공통 쿼리 빌더 생성 (SELECT, FROM, JOIN, WHERE)
        JPAQuery<UserResponseDto> query = queryFactory
                .select(new QUserResponseDto(
                        userEntity.id,
                        userEntity.userId,
                        userEntity.authorityId,
                        userEntity.userName,
                        userEntity.password,
                        userEntity.email,
                        userEntity.phone1,
                        userEntity.phone2,
                        userEntity.checkOutState,
                        userEntity.checkOutTime,
                        userEntity.checkOutUser,
                        userEntity.dataState,
                        userEntity.eventName,
                        userEntity.eventTime,
                        userEntity.eventUser,
                        userEntity.eventComment
                ))
                .from(userEntity)
                .where(
                        userIdContains(condition.getUserId()),
                        authorityIdEq(condition.getAuthorityId()),
                        userNameContains(condition.getUserName()),
                        passwordContains(condition.getPassword()),
                        emailContains(condition.getEmail()),
                        phone1Contains(condition.getPhone1())
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
        List<UserResponseDto> content = query.fetch();

        // 5. [수정] 카운트 조회 분기
        long total;
        if (pageable.isPaged()) {
            // [페이징 O] 별도 카운트 쿼리 실행
            Long count = queryFactory
                    .select(userEntity.count())
                    .from(userEntity)
                    .where(
                            // ** 동일한 WHERE 조건 **
                            userIdContains(condition.getUserId()),
                            authorityIdEq(condition.getAuthorityId()),
                            userNameContains(condition.getUserName()),
                            passwordContains(condition.getPassword()),
                            emailContains(condition.getEmail()),
                            phone1Contains(condition.getPhone1())
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
    public List<User> saveAll(List<User> user) {
        List<UserEntity> entityList = user.stream().map(userMapper::toEntity).collect(Collectors.toList());
        List<UserEntity> savedEntityList = userJpaRepository.saveAll(entityList);
        return savedEntityList.stream().map(userMapper::toDomain).collect(Collectors.toList());
    }

    /**
     * Pageable의 Sort 객체를 Querydsl의 OrderSpecifier 배열로 변환합니다.
     */
    private OrderSpecifier<?>[] getOrderSpecifiers(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();

        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                // 정렬 방향을 결정합니다 (ASC or DESC)
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;

                // 정렬할 속성(컬럼)을 PathBuilder를 통해 지정합니다.
                // "userName"과 같은 문자열을 Q-Type 경로로 변환해줍니다.
                PathBuilder pathBuilder = new PathBuilder<>(userEntity.getType(), userEntity.getMetadata());

                orders.add(new OrderSpecifier(direction, pathBuilder.get(order.getProperty())));
            }
        }

        // 기본 정렬 조건 (만약 정렬 조건이 없다면 id 내림차순)
        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier(Order.DESC, userEntity.id));
        }

        return orders.toArray(new OrderSpecifier[0]);
    }

    // == 동적 쿼리를 위한 BooleanExpression 메소드들 ==
    private BooleanExpression userIdContains(String userId) {
        return StringUtils.hasText(userId) ? userEntity.userId.contains(userId) : null;
    }

    // == 동적 쿼리를 위한 BooleanExpression 메소드들 ==
    private BooleanExpression authorityIdEq(Long authorityId) {
        return authorityId!=null ? userEntity.authorityId.eq(authorityId) : null;
    }

    private BooleanExpression userNameContains(String userName) {
        return StringUtils.hasText(userName) ? userEntity.userName.contains(userName) : null;
    }

    private BooleanExpression passwordContains(String password) {
        return StringUtils.hasText(password) ? userEntity.password.contains(password) : null;
    }

    private BooleanExpression emailContains(String email) {
        return StringUtils.hasText(email) ? userEntity.email.contains(email) : null;
    }

    private BooleanExpression phone1Contains(String phone1) {
        return StringUtils.hasText(phone1) ? userEntity.phone1.contains(phone1) : null;
    }


}
