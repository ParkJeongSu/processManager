package kr.co.aim.infra.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PURGE_CONFIG",catalog = "NEXBEPSM", schema = "dbo")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class PurgeConfigEntity {

    @Id
    @Column(name = "ID")
    private Integer id;

    @Column(name = "DB_NAME")
    private String dbName;

    @Column(name = "SCHEMA_NAME")
    private String schemaName;

    @Column(name = "TABLE_NAME")
    private String tableName;

    @Column(name = "TARGET_COLUMN_NAME")
    private String targetColumnName;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Column(name = "OPERATOR")
    private String operator;

    @Column(name = "COMP_VALUE")
    private String compValue;

    @Column(name = "BATCH_SIZE")
    private Integer batchSize;

    @Column(name = "MAX_LOOP_COUNT")
    private Integer maxLoopCount;

    @Column(name = "DELAY_MS")
    private Integer delayMs;

    @Column(name = "IS_ACTIVE")
    private String isActive;

    @Column(name = "LAST_RUN_TIME")
    private LocalDateTime lastRunTime;

}
