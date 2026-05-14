package kr.co.aim.api.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
@Slf4j
public class SchedulerAspect {

    // 1. kr.co.aim.api.schedule 패키지 하위의 모든 클래스 및 메서드를 타겟으로 잡음
    @Pointcut("within(kr.co.aim.api.schedule..*)")
    public void schedulerPointcut() {
    }

    // 2. 실행 전후에 로직을 삽입하는 Around 어드바이스
    @Around("schedulerPointcut()")
    public Object profileScheduler(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        // 1. Transaction ID 생성 (현재 시간을 ID로 사용)
        // LocalDateTime.now().toString()은 공백이나 특수문자가 포함될 수 있어 형식을 지정하는 것을 권장합니다.
        String transactionId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        // 2. MDC에 저장
        MDC.put("transactionId", transactionId);

        log.info("[Scheduler Start] -> Class: {}, Method: {}", className, methodName);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object result = null;

        try {
            // 실제 스케줄러 메서드 실행
            result = joinPoint.proceed();
        } catch (Throwable e) {
            // 에러 발생 시 로그 기록
            log.error("[Scheduler Error] -> Class: {}, Method: {}, Message: {}",
                    className, methodName, e.getMessage());
            // 에러를 다시 던져서 스케줄러가 에러 상황임을 인지하게 함
            throw e;
        } finally {
            stopWatch.stop();
            log.info("[Scheduler End] -> Class: {}, Method: {}, Execution Time: {}ms",
                    className, methodName, stopWatch.getTotalTimeMillis());
            MDC.clear();
        }

        return result;
    }
}
