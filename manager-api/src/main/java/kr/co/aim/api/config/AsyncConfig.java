package kr.co.aim.api.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // 전역 스위치 ON
public class AsyncConfig {
    // 여기에 아무런 코드를 적지 않아도 스프링은 프로젝트 전체의 @Async를 찾아냅니다.
}