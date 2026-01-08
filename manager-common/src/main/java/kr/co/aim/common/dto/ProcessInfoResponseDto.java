package kr.co.aim.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessInfoResponseDto {

    private Integer port;
    private String systemName;
    private String processGroupName;
    private String processName;
    private String description;
    private String copyDir;
    private String workingDir;
    private String fileName;
    private String command;

    @QueryProjection // ✨ 이 어노테이션이 있어야 QUserResponseDto가 생성됩니다.
    public ProcessInfoResponseDto(
            Integer port,
            String systemName,
            String processGroupName,
            String processName,
            String description,
            String copyDir,
            String workingDir,
            String fileName,
            String command
    ){
        this.port = port;
        this.systemName = systemName;
        this.processGroupName = processGroupName;
        this.processName = processName;
        this.description = description;
        this.copyDir = copyDir;
        this.workingDir = workingDir;
        this.fileName = fileName;
        this.command = command;
    }
}