package kr.co.aim.domain.command;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurgeLogUpdateCommand {
    private Long id;
    private Integer purgeConfigId;
    private String batchId;
    private String tableName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer deleteCount;
    private String status;
    private String errorMsg;
}
