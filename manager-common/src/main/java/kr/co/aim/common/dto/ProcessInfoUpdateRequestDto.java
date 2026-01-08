package kr.co.aim.common.dto;

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
    private String command;
}