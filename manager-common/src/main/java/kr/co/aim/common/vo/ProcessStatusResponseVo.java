package kr.co.aim.common.vo;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusResponseVo {
    private Integer port;
    private String systemName;
    private String processGroupName;
    private String processName;
    private String description;
    private String status;
    private Integer pid;
    private LocalDateTime startRequestTime;
    private LocalDateTime startTime;
    private LocalDateTime endRequestTime;
    private LocalDateTime endTime;
}
