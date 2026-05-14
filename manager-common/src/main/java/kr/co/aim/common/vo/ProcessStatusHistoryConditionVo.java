package kr.co.aim.common.vo;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessStatusHistoryConditionVo {
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