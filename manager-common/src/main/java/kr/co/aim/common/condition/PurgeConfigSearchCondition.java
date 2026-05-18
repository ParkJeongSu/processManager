package kr.co.aim.common.condition;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
public class PurgeConfigSearchCondition {

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