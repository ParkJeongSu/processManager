package kr.co.aim.domain.model;

import kr.co.aim.domain.command.ProcessInfoCreateCommand;
import kr.co.aim.domain.command.ProcessInfoUpdateCommand;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ProcessInfo {
    private Integer port;
    private String systemName;
    private String fileName;
    private String processGroupName;
    private String processName;
    private String description;
    private String copyDir;
    private String workingDir;
    private String batchDir;  // 예: C:\mng\
    private String batchName;  // 예: run.bat

    public static ProcessInfo create(ProcessInfoCreateCommand command){
        return ProcessInfo.builder()
                .port(command.getPort())
                .systemName(command.getSystemName())
                .fileName(command.getFileName())
                .processGroupName(command.getProcessGroupName())
                .processName(command.getProcessName())
                .description(command.getDescription())
                .copyDir(command.getCopyDir())
                .workingDir(command.getWorkingDir())
                .batchDir(command.getBatchDir())
                .batchName(command.getBatchName())
                .build();
    }

    public void changeProcessInfo(ProcessInfoUpdateCommand command){
        this.setSystemName(command.getSystemName());
        this.setFileName(command.getFileName());
        this.setProcessGroupName(command.getProcessGroupName());
        this.setProcessName(command.getProcessName());
        this.setDescription(command.getDescription());
        this.setCopyDir(command.getCopyDir());
        this.setWorkingDir(command.getWorkingDir());
        this.setBatchDir(command.getBatchDir());
        this.setBatchName(command.getBatchName());
    }
}
