package kr.co.aim.infra.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "kr.co.aim.infra.persistence.db2springdatajpa", // 1. 이 설정은 이 패키지의 리포지토리를 담당!
        entityManagerFactoryRef = "db2EntityManagerFactory", // 2. 사용할 EntityManagerFactory 지정
        transactionManagerRef = "db2TransactionManager"      // 3. 사용할 TransactionManager 지정
)
public class DB2DataSourceConfig {

    @Value("${spring.datasource.db2.schema-name}")
    private String db2Schema;

    // ★ YAML에서 테스트 쿼리 문자열만 명시적으로 가져옵니다.
    @Value("${spring.datasource.db2.hikari.connection-test-query}")
    private String db2ConnectionTestQuery;

    // ★ YAML에서 방언 문자열을 동적으로 주입받습니다.
    @Value("${spring.datasource.db2.dialect}")
    private String db2Dialect;

    @Bean(name = "db2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource db2DataSource() {

        // HikariDataSource 타입으로 명시적 생성하여 빌드합니다.
        HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        // 2. ★ 다른 설정은 자동 주입되게 두고, 테스트 쿼리만 가져온 변수로 명시하여 덮어씁니다.
        dataSource.setConnectionTestQuery(db2ConnectionTestQuery);
        return dataSource;
    }

    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("db2DataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        // DB가 죽어있어도 메타데이터 조회를 건너뜀
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        // 하드코딩 대신 주입받은 변수를 사용합니다.
        properties.put("hibernate.default_schema", db2Schema);
        // 커넥션 오류 시 발생하는 Dialect 예외를 방지하기 위해 명시적으로 설정을 추가합니다.
        properties.put("hibernate.dialect", db2Dialect);
        return builder
                .dataSource(dataSource)
                .packages("kr.co.aim.infra.persistence.db2entity") // 4. 이 EntityManager는 이 패키지의 엔티티만 스캔!
                .properties(properties) // 설정 추가
                .build();
    }

    @Bean(name = "db2TransactionManager")
    public PlatformTransactionManager db2TransactionManager(
            @Qualifier("db2EntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Bean(name = "db2JdbcTemplate")
    public JdbcTemplate db2JdbcTemplate(@Qualifier("db2DataSource")DataSource db2DataSource) {
        return new JdbcTemplate(db2DataSource);
    }
}