package kr.co.aim.domain.model;


import kr.co.aim.common.Utils.TsidUtils;
import kr.co.aim.domain.command.PurgeLogCreateCommand;
import lombok.*;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class PurgeLog {

    private Long id;
    private Integer purgeConfigId;
    private String batchId;
    private String tableName;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Integer deleteCount;
    private String status;
    private String errorMsg;

    public static PurgeLog create(PurgeLogCreateCommand command){
        return PurgeLog
                .builder()
                .id(TsidUtils.nextId())
                .purgeConfigId(command.getPurgeConfigId())
                .batchId(command.getBatchId())
                .tableName(command.getTableName())
                .startDateTime(command.getStartDateTime())
                .endDateTime(command.getEndDateTime())
                .deleteCount(ObjectUtils.isEmpty(command.getDeleteCount()) ? 0 : command.getDeleteCount()  )
                .status(command.getStatus())
                .errorMsg(command.getErrorMsg())
                .build();
    }

}
