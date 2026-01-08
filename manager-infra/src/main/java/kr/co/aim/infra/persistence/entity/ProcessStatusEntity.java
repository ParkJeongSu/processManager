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
@Table(name = "PROCESS_STATUS")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class ProcessStatusEntity {

    @Id
    private Integer port; // 예: 8081

    @Column(name = "PROCESS_NAME")
    private String processName; // 예: pex11

    @Column(name = "STATUS")
    private String status;

    @Column(name = "PID")
    private Integer pid; // 예: 844512

    @Column(name = "START_REQUEST_TIME")
    private LocalDateTime startRequestTime;

    @Column(name = "START_TIME")
    private LocalDateTime startTime;

    @Column(name = "END_REQUEST_TIME")
    private LocalDateTime endRequestTime;

    @Column(name = "END_TIME")
    private LocalDateTime endTime;

}
