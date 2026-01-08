package kr.co.aim.common.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class UserSearchConditionDto {
    private String userId;
    private Long authorityId;
    private String userName;
    private String password;
    private String email;
    private String phone1;
}