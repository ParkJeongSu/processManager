package kr.co.aim.common.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
public class UserUpdateRequestDto {

    private Long id;
    private String userId;
    private Long authorityId;
    private String authorityName;
    private String userName;
    private String password;
    private String email;
    private String phone1;
    private String phone2;
    private String checkOutState;
    private LocalDateTime checkOutTime;
    private String checkOutUser;
    private String dataState;
    private String eventName;
    private LocalDateTime eventTime;
    private String eventUser;
    private String eventComment;
}