package kr.co.aim.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "kr.co.aim.infra.persistence.springdatajpa", // 1. 담당할 리포지토리 패키지 지정
        entityManagerFactoryRef = "mssqlEntityManagerFactory",
        transactionManagerRef = "mssqlTransactionManager"
)
public class MSSQLDataSourceConfig {

    @Bean(name = "mssqlDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.mssql")
    public DataSource mssqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = "mssqlEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean mssqlEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("mssqlDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("kr.co.aim.infra.persistence.entity") // 2. 담당할 엔티티 패키지 지정
                .build();
    }

    @Primary
    @Bean(name = "mssqlTransactionManager")
    public PlatformTransactionManager mssqlTransactionManager(
            @Qualifier("mssqlEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }

    @Primary
    @Bean(name = "mssqlJdbcTemplate")
    public JdbcTemplate mssqlJdbcTemplate(@Qualifier("mssqlDataSource")DataSource db2DataSource) {
        return new JdbcTemplate(db2DataSource);
    }

}