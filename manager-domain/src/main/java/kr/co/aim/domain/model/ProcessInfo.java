package kr.co.aim.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
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
    private String processGroupName;
    private String processName;
    private String description;
    private String copyDir;
    private String workingDir;
    private String fileName;
    private String command;

    public static ProcessInfo create(ProcessInfoCreateCommand command){
        return ProcessInfo.builder()
                .port(command.getPort())
                .systemName(command.getSystemName())
                .processGroupName(command.getProcessGroupName())
                .processName(command.getProcessName())
                .description(command.getDescription())
                .copyDir(command.getCopyDir())
                .workingDir(command.getWorkingDir())
                .fileName(command.getFileName())
                .command(command.getCommand())
                .build();
    }

    public void changeProcessInfo(ProcessInfoUpdateCommand command){
        this.setSystemName(command.getSystemName());
        this.setProcessGroupName(command.getProcessGroupName());
        this.setProcessName(command.getProcessName());
        this.setDescription(command.getDescription());
        this.setCopyDir(command.getCopyDir());
        this.setWorkingDir(command.getWorkingDir());
        this.setFileName(command.getFileName());
        this.setCommand(command.getCommand());
    }
}
