package kr.co.aim.api.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect // AOP Aspect 클래스임을 선언
@Component
@RequiredArgsConstructor
public class HistoryEventAspect {
    private final ApplicationEventPublisher eventPublisher;

    // 1. @PublishHistoryEvent 어노테이션이 달린 모든 메서드를 Pointcut으로 지정
    @Pointcut("@annotation(kr.co.aim.common.annotation.PublishHistoryEvent)")
    public void publishHistoryEventPointcut() {
    }

    /*
    // @PublishHistoryEvent 어노테이션이 붙은 메소드가 '성공적으로 반환된 후' 이 로직을 실행
    @AfterReturning(value = "publishHistoryEventPointcut()", returning = "result")
    public void publishHistoryEvent(Object result) {
        if (result instanceof Alarm) {
            Alarm savedAlarm = (Alarm) result;
            log.info("AOP: Alarm has been saved. Publishing history event for ID: {}", savedAlarm.getId());
            eventPublisher.publishEvent(new AlarmHistoryEvent(savedAlarm));
        }
        // else if (result instanceof User) { ... } 등 다른 엔티티도 추가 가능
    }
    */

    // 2. Pointcut으로 지정된 메서드 실행 전후에 Around Advice 적용
    @Around("publishHistoryEventPointcut()")
    public Object publishHistoryEvent(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }




}
