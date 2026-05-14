package kr.co.aim.common.vo;


import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurgeLogVo {

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
