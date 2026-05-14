package kr.co.aim.api.dto;

import kr.co.aim.common.vo.ProcessInfoCreateRequestVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessInfoCreateRequestDto {

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

    public static ProcessInfoCreateRequestVo toVo(ProcessInfoCreateRequestDto requestDto) {
        return ProcessInfoCreateRequestVo
                .builder()
                .port(requestDto.port)
                .systemName(requestDto.systemName)
                .processGroupName(requestDto.processGroupName)
                .processName(requestDto.processName)
                .description(requestDto.description)
                .copyDir(requestDto.copyDir)
                .workingDir(requestDto.workingDir)
                .fileName(requestDto.fileName)
                .batchDir(requestDto.batchDir)
                .batchName(requestDto.batchName)
                .build();
    }
}