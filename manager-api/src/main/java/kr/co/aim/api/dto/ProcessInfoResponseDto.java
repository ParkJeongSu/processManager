package kr.co.aim.api.dto;

import kr.co.aim.domain.model.ProcessInfo;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
public class ProcessInfoResponseDto {

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

    public static ProcessInfoResponseDto from(ProcessInfo processInfo){
        return ProcessInfoResponseDto
                .builder()
                .port(processInfo.getPort())
                .systemName(processInfo.getSystemName())
                .processGroupName(processInfo.getProcessGroupName())
                .processName(processInfo.getProcessName())
                .description(processInfo.getDescription())
                .copyDir(processInfo.getCopyDir())
                .workingDir(processInfo.getWorkingDir())
                .fileName(processInfo.getFileName())
                .build();
    }
}