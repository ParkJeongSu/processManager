package kr.co.aim.infra.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
@ToString(of = {"id", "userName", "email"}) // 연관관계 필드는 제외하고 출력
@AllArgsConstructor
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String userId;
    private Long authorityId;
    private String userName;
    private String password;
    private String email;
    private String phone1;
    private String phone2;
    @Column(length = 255) // Refresh Token은 길이가 길 수 있으므로 길이를 넉넉하게 설정
    private String refreshToken;
    private String checkOutState;
    
    private LocalDateTime checkOutTime;
    private String checkOutUser;
    private String dataState;
    private String eventName;
    
    
    private LocalDateTime eventTime;
    private String eventUser;
    private String eventComment;
}