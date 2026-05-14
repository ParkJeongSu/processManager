package kr.co.aim.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
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

    @Bean(name = "db2DataSource")
    @ConfigurationProperties(prefix = "spring.datasource.db2")
    public DataSource db2DataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "db2EntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean db2EntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("db2DataSource") DataSource dataSource) {
        Map<String, Object> properties = new HashMap<>();
        // DB가 죽어있어도 메타데이터 조회를 건너뜀
        properties.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        // DB2 방언을 명시적으로 지정 (DB 접속을 안 해도 Hibernate가 동작하게 함)
        properties.put("hibernate.dialect", "org.hibernate.dialect.DB2Dialect");
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