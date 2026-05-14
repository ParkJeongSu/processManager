package kr.co.aim.api.dto;

import kr.co.aim.common.vo.ProcessInfoUpdateRequestVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessInfoUpdateRequestDto {

    private Integer port;
    private String systemName;
    private String processGroupName;
    private String processName;
    private String description;
    private String copyDir;
    private String workingDir;
    private String fileName;
    private String batchDir;  // 예: C:\mng\
    private String batchName;  // 예: run.bat

    public static ProcessInfoUpdateRequestVo toVo(ProcessInfoUpdateRequestDto dto){
        return ProcessInfoUpdateRequestVo
                .builder()
                .port(dto.getPort())
                .systemName(dto.getSystemName())
                .processGroupName(dto.getProcessGroupName())
                .processName(dto.getProcessName())
                .description(dto.getDescription())
                .copyDir(dto.getCopyDir())
                .workingDir(dto.getWorkingDir())
                .fileName(dto.getFileName())
                .batchDir(dto.getBatchDir())
                .batchName(dto.getBatchName())
                .build();
    }
}