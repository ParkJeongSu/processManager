package kr.co.aim.domain.model;

import kr.co.aim.common.handler.HasTransactionInfo;
import kr.co.aim.domain.command.UserCreateCommand;
import kr.co.aim.domain.command.UserUpdateCommand;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor//(access = AccessLevel.PROTECTED) // JPA Entity 등을 위한 기본 생성자
@AllArgsConstructor
@Builder
public class User implements HasTransactionInfo {
    private Long id;
    private String userId;
    private Long authorityId;
    private String userName;
    private String password;
    private String email;
    private String phone1;
    private String phone2;
    private String refreshToken;
    private String checkOutState;
    private LocalDateTime checkOutTime;
    private String checkOutUser;
    private String dataState;
    private String eventName;

    private LocalDateTime eventTime;
    private String eventUser;
    private String eventComment;

    // 비즈니스 로직 예시
    public void changeUsername(String newUsername) {
        if (newUsername == null || newUsername.isBlank()) {
            throw new IllegalArgumentException("사용자 이름은 비워둘 수 없습니다.");
        }
        this.userName = newUsername;
    }

    public static User create(UserCreateCommand command){
        return User.builder()
                .authorityId(command.getAuthorityId())
                .userId(command.getUserId())
                .userName(command.getUserName())
                .email(command.getEmail())
                .password(command.getPassword())
                .phone1(command.getPhone1())
                .phone2(command.getPhone2())
                .eventName(command.getTransactionInfo().eventName())
                .eventComment(command.getTransactionInfo().eventComment())
                .eventUser(command.getTransactionInfo().eventUser())
                .eventTime(command.getTransactionInfo().eventTime())
                .build();
    }

    // Refresh Token을 업데이트하는 메서드
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void changeUser(UserUpdateCommand command){
        this.apply(command.getTransactionInfo());
        this.setAuthorityId(command.getAuthorityId());
        this.setUserName(command.getUserName());
        this.setEmail(command.getEmail());
        this.setPassword(command.getPassword());
        this.setPhone1(command.getPhone1());
        this.setPhone2(command.getPhone2());
    }
}