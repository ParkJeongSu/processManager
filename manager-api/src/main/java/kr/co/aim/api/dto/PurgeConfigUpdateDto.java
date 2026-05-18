package kr.co.aim.api.dto;


import kr.co.aim.common.Utils.TsidUtils;
import kr.co.aim.domain.command.PurgeConfigUpdateCommand;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PurgeConfigUpdateDto {

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

    public static PurgeConfigUpdateCommand toPurgeConfigUpdateCommand(PurgeConfigUpdateDto dto) {
        return PurgeConfigUpdateCommand
                .builder()
                .id(TsidUtils.nextId().intValue())
                .dbName(dto.getDbName())
                .schemaName(dto.getSchemaName())
                .tableName(dto.getTableName())
                .targetColumnName(dto.getTargetColumnName())
                .dataType(dto.getDataType())
                .operator(dto.getOperator())
                .compValue(dto.getCompValue())
                .batchSize(dto.getBatchSize())
                .maxLoopCount(dto.getMaxLoopCount())
                .delayMs(dto.getDelayMs())
                .isActive(dto.getIsActive())
                .lastRunTime(dto.getLastRunTime())
                .build();
    }

}
