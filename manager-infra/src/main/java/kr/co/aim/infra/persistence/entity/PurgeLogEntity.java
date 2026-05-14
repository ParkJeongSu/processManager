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
@Table(name = "PURGE_LOG",catalog = "NEXBEPSM", schema = "dbo")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class PurgeLogEntity {

    @Id
    @Column(name = "ID")
    private Long id;

    @Column(name = "PURGE_CONFIG_ID")
    private Integer purgeConfigId;

    @Column(name = "BATCH_ID")
    private String batchId;

    @Column(name = "TABLE_NAME")
    private String tableName;

    @Column(name = "START_DATE_TIME")
    private LocalDateTime startDateTime;

    @Column(name = "END_DATE_TIME")
    private LocalDateTime endDateTime;

    @Column(name = "DELETE_COUNT")
    private Integer deleteCount;

    @Column(name = "STATUS", length = 30)
    private String status;

    @Column(name = "ERROR_MSG")
    private String errorMsg;

}
