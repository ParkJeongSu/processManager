package kr.co.aim.domain.model;


import kr.co.aim.common.Utils.TsidUtils;
import kr.co.aim.domain.command.ProcessInfoCreateCommand;
import kr.co.aim.domain.command.ProcessStatusCreateCommand;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProcessStatus {

    private Long id;
    private Integer port; // 예: 8081
    private String processName; // 예: pex11
    private String status;
    private Integer pid; // 예: 844512
    private String lastEventUser;
    private LocalDateTime startRequestTime;
    private LocalDateTime startTime;
    private LocalDateTime endRequestTime;
    private LocalDateTime endTime;

    public static ProcessStatus create(ProcessStatusCreateCommand command){
        return ProcessStatus.builder()
                .id(TsidUtils.nextId())
                .port(command.getPort())
                .processName(command.getProcessName())
                .status(command.getStatus())
                .pid(command.getPid())
                .lastEventUser(command.getLastEventUser())
                .startRequestTime(command.getStartRequestTime())
                .startTime(command.getStartTime())
                .endRequestTime(command.getEndRequestTime())
                .endTime(command.getEndTime())
                .build();
    }

}
