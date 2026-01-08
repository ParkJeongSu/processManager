package kr.co.aim.common.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@Builder
public class UserResponseDto {
    private Long id;
    private String userId;
    private Long authorityId;
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

    @QueryProjection // ✨ 이 어노테이션이 있어야 QUserResponseDto가 생성됩니다.
    public UserResponseDto(Long id,
                           String userId,
                           Long authorityId,
                           String userName,
                           String password,
                           String email,
                           String phone1,
                           String phone2,
                           String checkOutState,
                           LocalDateTime checkOutTime,
                           String checkOutUser,
                           String dataState,
                           String eventName,
                           
                           LocalDateTime eventTime,
                           String eventUser,
                           String eventComment) {
        this.id = id;
        this.userId = userId;
        this.authorityId = authorityId;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.checkOutState = checkOutState;
        this.checkOutTime = checkOutTime;
        this.checkOutUser = checkOutUser;
        this.dataState = dataState;
        this.eventName = eventName;
        this.eventTime = eventTime;
        this.eventUser = eventUser;
        this.eventComment = eventComment;
    }

}