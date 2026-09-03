package kr.co.aim.domain.model;

import jakarta.persistence.Column;
import kr.co.aim.common.Utils.TsidUtils;
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
    private Long id;
    private Integer port; // 예: 8081
    private String systemName; // 예: pex11
    private String fileName;  // 예: mng.jar
    private String processGroupName; // 예: pex
    private String processName; // 예: pex11
    private String description;
    private String copyDir;  // 예: C:\mng\pex11
    private String workingDir;  // 예: C:\mng\pex11
    private String batchDir;  // 예: C:\mng\
    private String batchName;  // 예: run.bat
    private String stopBatchName;  // 예: stop.bat

    public static ProcessInfo create(ProcessInfoCreateCommand command){
        return ProcessInfo.builder()
                .id(TsidUtils.nextId())
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
                .stopBatchName(command.getStopBatchName())
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
        this.setStopBatchName(command.getStopBatchName());
    }
}
