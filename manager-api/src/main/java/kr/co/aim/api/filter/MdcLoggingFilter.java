package kr.co.aim.api.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // 이 필터가 가장 먼저 실행되도록 순서 지정
@Slf4j
public class MdcLoggingFilter implements Filter {

    // logback.xml의 %X{traceId}와 일치시켜야 합니다.
    private static final String MDC_KEY = "transactionId";

    // 외부 시스템(API 게이트웨이 등)과 ID를 주고받을 때 사용할 HTTP 헤더 이름
    private static final String TRACE_ID_HEADER = "X-Trace-ID";

    // 제외하고 싶은 경로 정의
    private static final String EXCLUDE_PATH = "/actuator";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String uri = httpServletRequest.getRequestURI();

        // 1. 프로메테우스 수집 및 헬스 체크 경로(Actuator)는 필터 로직 제외
        if (uri.startsWith(EXCLUDE_PATH)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 2. MDC 설정
            String traceId = httpServletRequest.getHeader(TRACE_ID_HEADER);
            if (!StringUtils.hasText(traceId)) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put(MDC_KEY, traceId);

            // 3. 비즈니스 로그 출력
            log.info("business logic start [URI: {}]", uri);

            chain.doFilter(request, response);

        } finally {
            // 4. 종료 로그 및 MDC 정리
            log.info("business logic end");
            MDC.clear();
        }
    }

    // init, destroy 메서드는 기본 구현을 사용하므로 생략 가능합니다.
}