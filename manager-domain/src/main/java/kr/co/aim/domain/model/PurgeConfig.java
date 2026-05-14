package kr.co.aim.domain.model;


import kr.co.aim.common.Utils.TsidUtils;
import kr.co.aim.domain.command.PurgeConfigCreateCommand;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PurgeConfig {

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

    public static PurgeConfig create(PurgeConfigCreateCommand command) {
        return PurgeConfig
                .builder()
                .id(TsidUtils.nextId().intValue())
                .dbName(command.getDbName())
                .schemaName(command.getSchemaName())
                .tableName(command.getTableName())
                .targetColumnName(command.getTargetColumnName())
                .dataType(command.getDataType())
                .operator(command.getOperator())
                .compValue(command.getCompValue())
                .batchSize(command.getBatchSize())
                .maxLoopCount(command.getMaxLoopCount())
                .delayMs(command.getDelayMs())
                .isActive(command.getIsActive())
                .lastRunTime(command.getLastRunTime())
                .build();
    }

}
