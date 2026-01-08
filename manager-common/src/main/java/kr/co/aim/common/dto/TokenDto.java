package kr.co.aim.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String grantType; // JWT에 대한 인증 타입 (여기서는 Bearer 사용)
    private String accessToken;
    private String refreshToken; // refreshToken 필드 추가
}