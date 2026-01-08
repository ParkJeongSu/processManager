package kr.co.aim.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@Builder
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

    @QueryProjection
    public ProcessStatusHistoryResponseDto(
            LocalDateTime eventTime,
            Integer port,
            String processName,
            String status,
            Integer pid,
            LocalDateTime startRequestTime,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String eventUser
    )
    {
        this.eventTime = eventTime;
        this.port = port;
        this.processName = processName;
        this.status = status;
        this.pid = pid;
        this.startRequestTime = startRequestTime;
        this.startTime = startTime;
        this.endTime = endTime;
        this.eventUser = eventUser;
    }
}