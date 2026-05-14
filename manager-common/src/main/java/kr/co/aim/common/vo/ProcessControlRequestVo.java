package kr.co.aim.common.vo;


import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
public class ProcessControlRequestVo {
    private String userId;
    private String eventName;
    private LocalDateTime eventTime;
    private String eventUser;
    private String eventComment;

}