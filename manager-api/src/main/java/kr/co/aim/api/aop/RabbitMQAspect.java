package kr.co.aim.api.aop;


import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.aim.common.format.request.MessageHeader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQAspect {
    private final ObjectMapper objectMapper;

    // 1. @RabbitListener 어노테이션이 달린 모든 메서드를 Pointcut으로 지정
    @Pointcut("@annotation(org.springframework.amqp.rabbit.annotation.RabbitListener)")
    public void rabbitListenerPointcut() {
    }

    //     2. Pointcut으로 지정된 메서드 실행 전후에 Around Advice 적용
    @Around("rabbitListenerPointcut()")
    public Object setMdcAroundRabbitListener(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Object[] args = joinPoint.getArgs();

            if (args != null && args.length > 0 && args[0] instanceof org.springframework.amqp.core.Message) {
                try {
                    // 1. Message 객체로 캐스팅
                    org.springframework.amqp.core.Message message = (org.springframework.amqp.core.Message) args[0];

                    // 2. 바디(byte[]) 추출
                    byte[] body = message.getBody();

                    // 3. ObjectMapper로 필요한 헤더 정보만 읽기
                    MessageHeader header = objectMapper.readValue(body, MessageHeader.class);

                    if (ObjectUtils.isNotEmpty( header.getTransactionId())) {
                        MDC.put("transactionId", header.getTransactionId());
                    }
                } catch (Exception e) {
                    // 파싱 실패 시 로깅 (상세 에러 확인을 위해 e.getMessage() 추가 권장)
                    log.info("MDC set error: " + e.getMessage());
                }
            }

            log.info("business logic start");
            return joinPoint.proceed();

        } catch (Exception e) {
            log.error("Aspect logic error: " + e.getMessage());
            throw e;
        } finally {
            log.info("business logic end");
            MDC.clear();
        }
    }

    /* X{traceId} 를 이용해 mdc 패턴 사용
        <configuration>
            <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
                <encoder>
                    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%X{traceId}] - %msg%n</pattern>
                </encoder>
            </appender>

            <root level="INFO">
                <appender-ref ref="CONSOLE"/>
            </root>
        </configuration>
    * */

}
