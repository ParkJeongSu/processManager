package kr.co.aim.common.condition;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurgeLogSearchCondition {
    private LocalDateTime fromEventTime;
    private LocalDateTime toEventTime;
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
