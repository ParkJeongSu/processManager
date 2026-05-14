package kr.co.aim.api.dto;

import kr.co.aim.common.vo.ProcessStatusResponseVo;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusResponseDto {
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

    public static ProcessStatusResponseVo toVo(ProcessStatusResponseDto dto) {
        return ProcessStatusResponseVo
                .builder()
                .port(dto.getPort())
                .systemName(dto.getSystemName())
                .processGroupName(dto.getProcessGroupName())
                .processName(dto.getProcessName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .pid(dto.getPid())
                .startRequestTime(dto.getStartRequestTime())
                .startTime(dto.getStartTime())
                .endRequestTime(dto.getEndRequestTime())
                .endTime(dto.getEndTime())
                .build();
    }

    public static ProcessStatusResponseDto from (ProcessStatusResponseVo dto) {
        return ProcessStatusResponseDto
                .builder()
                .port(dto.getPort())
                .systemName(dto.getSystemName())
                .processGroupName(dto.getProcessGroupName())
                .processName(dto.getProcessName())
                .description(dto.getDescription())
                .status(dto.getStatus())
                .pid(dto.getPid())
                .startRequestTime(dto.getStartRequestTime())
                .startTime(dto.getStartTime())
                .endRequestTime(dto.getEndRequestTime())
                .endTime(dto.getEndTime())
                .build();
    }
}
