package kr.co.aim.api.config;
import kr.co.aim.api.jwt.JwtAuthenticationFilter;
import kr.co.aim.api.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider; // JwtTokenProvider 주입

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
        // httpBasic, csrf, formLogin, rememberMe, logout, session 비활성화
        .httpBasic(AbstractHttpConfigurer::disable)
        .csrf(AbstractHttpConfigurer::disable)
        .formLogin(AbstractHttpConfigurer::disable)
        .rememberMe(AbstractHttpConfigurer::disable)
        .logout(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

        // 요청에 대한 인가 규칙 설정
        .authorizeHttpRequests(authorize -> authorize
                //.requestMatchers("/api/members/login", "/api/members/join","/api/members/reissue").permitAll() // 로그인, 회원가입 경로는 인증 없이 접근 허용
                //.anyRequest().authenticated()
                .anyRequest().permitAll()
                )
                // ↓↓↓↓↓↓ 필터 추가 부분 ↓↓↓↓↓↓
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        // 테스트 기간동안 모든 요청 허용
        // 테스트 기간이 끝나면, .anyRequest().permitAll() 를 제거하고 위의 두줄을 다시 주석 해제
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt Encoder 사용
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}