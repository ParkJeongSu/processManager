package kr.co.aim.infra.config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"pex","tex","scheduler"})
public class RabbitMQRetryConfig {
    @Autowired
    private ConnectionFactory connectionFactory;

    // SimpleRabbitListenerContainerFactory 빈을 직접 수정하여 Advice를 적용합니다.
    // 기존의 factory 빈이 있다면 이 메서드가 오버라이드하게 됩니다.
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        // --- 이 부분을 추가하세요 ---
        // 예외 발생 시 메시지를 큐에 다시 넣지 않도록 설정
        factory.setDefaultRequeueRejected(false);
        factory.setContainerCustomizer(
                c-> {
                    c.setShutdownTimeout(60_000L);
                }
        );
        factory.setAdviceChain(retryInterceptor());

        return factory;
    }

    @Bean
    public Advice retryInterceptor() {

        return RetryInterceptorBuilder.stateless()
                .maxAttempts(5)                    // "총 5회 시도" (첫 호출 + 재시도 4회)
                //.backOffOptions(1000, 2.0, 10000) // 선택: 재시도 간격(초기 1s, 배수 2.0, 최대 10s)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }
}
