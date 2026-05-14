package kr.co.aim.api.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
public class ProcessStatusHistoryResponseDto {
    private LocalDateTime eventTime;
    private Integer port; // 예: 8081
    private String processName; // 예: pex11
    private String status;
    private Integer pid; // 예: 8081
    private LocalDateTime startRequestTime;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String eventUser;
}