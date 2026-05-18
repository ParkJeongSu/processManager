package kr.co.aim.api.config;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
// 1. 새로운 설정 객체(Settings) 생성 및 타임아웃 설정
        // 이 과정에서 람다식을 배제하고 Builder 패턴의 메서드 체이닝을 활용합니다.
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(Duration.ofSeconds(10))
                .withReadTimeout(Duration.ofSeconds(60));

        // 2. 작성된 설정을 RestTemplateBuilder에 주입하여 빌드
        return builder
                .requestFactorySettings(settings)
                .build();
    }
}
