package kr.co.aim.infra.persistence.adapter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import kr.co.aim.domain.repository.DB2ConnectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * UserRepository의 JPA 기반 구현체.
 * 실제 DB 작업은 Spring Data JPA가 제공하는 JpaRepository에 위임합니다.
 */

@Repository
@RequiredArgsConstructor
public class DB2ConnectionRepositoryImpl implements DB2ConnectionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // ★ YAML에서 테스트 쿼리 문자열만 명시적으로 가져옵니다.
    @Value("${spring.datasource.db2.hikari.connection-test-query}")
    private String db2ConnectionTestQuery;

    @Override
    public Integer checkConnection() {
        // YAML에서 읽어온 쿼리 문자열을 그대로 네이티브 쿼리로 실행합니다.
        Object result = entityManager.createNativeQuery(db2ConnectionTestQuery).getSingleResult();

        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        return 1; // DB2i(AS400) 등에서 결과 타입이 다르게 떨어질 경우를 대비한 안전한 처리
    }
}
