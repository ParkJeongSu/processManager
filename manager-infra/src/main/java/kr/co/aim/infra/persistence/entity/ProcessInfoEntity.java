package kr.co.aim.infra.persistence.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PROCESS_INFO")
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class ProcessInfoEntity {

    @Id
    private Integer port; // 예: 8081

    @Column(name = "SYSTEM_NAME")
    private String systemName; // 예: pex11

    @Column(name = "PROCESS_GROUP_NAME")
    private String processGroupName; // 예: pex

    @Column(name = "PROCESS_NAME")
    private String processName; // 예: pex11

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "COPY_DIR")
    private String copyDir;  // 예: C:\mng\pex11

    @Column(name = "WORKING_DIR")
    private String workingDir;  // 예: C:\mng\pex11

    @Column(name = "FILE_NAME")
    private String fileName;  // 예: mng.jar

    @Column(name = "COMMAND")
    private String command;  // 예: javaw -jar
}
