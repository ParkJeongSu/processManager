package kr.co.aim.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessStatusHistoryConditionDto {
    private LocalDateTime fromEventTime;
    private LocalDateTime toEventTime;
    private Integer port; // 예: 8081
    private String processName; // 예: pex11
    private LocalDateTime startRequestTime;
    private LocalDateTime startTime;
    private LocalDateTime endRequestTime;
    private LocalDateTime endTime;
    private String eventUser;
}