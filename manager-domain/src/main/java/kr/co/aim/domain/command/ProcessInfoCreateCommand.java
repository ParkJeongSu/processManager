package kr.co.aim.domain.command;

import kr.co.aim.common.record.TransactionInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
@Builder
public class ProcessInfoCreateCommand {
    private final Integer port;
    private final String systemName;
    private final String processGroupName;
    private final String processName;
    private final String description;
    private final String copyDir;
    private final String workingDir;
    private final String fileName;
    private final String command;
}
