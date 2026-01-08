package kr.co.aim.api.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect // AOP Aspect 클래스임을 선언
@Component
@RequiredArgsConstructor
public class NotificationHandlerAspect {

    // 1. @RabbitListener 어노테이션이 달린 모든 메서드를 Pointcut으로 지정
//    @Pointcut("@annotation(kr.co.aim.common.annotation.PublishHistoryEvent)")
//    public void publishHistoryEventPointcut() {
//    }

    // @PublishHistoryEvent 어노테이션이 붙은 메소드가 '성공적으로 반환된 후' 이 로직을 실행
    // NotificationHandler를 구현(상속)한 모든 타입(+)의 모든 메서드(*)를 매칭
    @Around("execution(* kr.co.aim.common.handler..NotificationHandler+.*(..))"
            + " && !execution(* java.lang.Object.*(..))") // toString/equals 등 제외(선택)
    public Object NotificationHandlerEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            log.info("NotificationHandlerEvent logic start");
            return joinPoint.proceed();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            // 추가적으로 로그 더 기록
        } finally {
            log.info("NotificationHandlerEvent logic end");
        }
        return null;
    }
}
