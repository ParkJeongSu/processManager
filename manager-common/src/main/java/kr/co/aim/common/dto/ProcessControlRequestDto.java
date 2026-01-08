package kr.co.aim.common.dto;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class ProcessControlRequestDto {
    private String userId;
    private String eventName;
    private LocalDateTime eventTime;
    private String eventUser;
    private String eventComment;
}