package kr.co.aim.common.vo;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
public class ProcessInfoUpdateRequestVo {

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
}