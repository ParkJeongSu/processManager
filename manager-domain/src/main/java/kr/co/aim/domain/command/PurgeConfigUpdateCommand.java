package kr.co.aim.domain.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PurgeConfigUpdateCommand {
    private Integer id;
    private String dbName;
    private String schemaName;
    private String tableName;
    private String targetColumnName;
    private String dataType;
    private String operator;
    private String compValue;
    private Integer batchSize;
    private Integer maxLoopCount;
    private Integer delayMs;
    private String isActive;
    private LocalDateTime lastRunTime;
}
