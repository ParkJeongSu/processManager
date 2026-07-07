package kr.co.aim.domain.command;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Builder
public class ProcessStatusCreateCommand {
    private final Long id;
    private final Integer port; // 예: 8081
    private final String processName; // 예: pex11
    private final String status;
    private final Integer pid; // 예: 844512
    private final String lastEventUser;
    private final LocalDateTime startRequestTime;
    private final LocalDateTime startTime;
    private final LocalDateTime endRequestTime;
    private final LocalDateTime endTime;
}
