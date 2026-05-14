package kr.co.aim.api.dto;

import kr.co.aim.common.vo.ProcessInfoSearchConditionVo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessInfoSearchConditionDto {

    private Integer port;
    private String systemName;
    private String processGroupName;
    private String processName;
    private String description;
    private String copyDir;
    private String workingDir;
    private String fileName;
    private String command;

    public static ProcessInfoSearchConditionVo toVo(ProcessInfoSearchConditionDto condition) {
        return ProcessInfoSearchConditionVo
                .builder()
                .port(condition.getPort())
                .systemName(condition.getSystemName())
                .processGroupName(condition.getProcessGroupName())
                .processName(condition.getProcessName())
                .description(condition.getDescription())
                .copyDir(condition.getCopyDir())
                .workingDir(condition.getWorkingDir())
                .fileName(condition.getFileName())
                .command(condition.getCommand())
                .build();
    }
}