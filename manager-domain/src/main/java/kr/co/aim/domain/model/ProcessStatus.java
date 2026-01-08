package kr.co.aim.domain.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProcessStatus {

    private Integer port; // 예: 8081
    private String processName; // 예: pex11
    private String status;
    private Integer pid; // 예: 844512
    private LocalDateTime startRequestTime;
    private LocalDateTime startTime;
    private LocalDateTime endRequestTime;
    private LocalDateTime endTime;

}
