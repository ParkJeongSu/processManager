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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            // HTTP 요청이 아닌 경우를 대비
            if (request instanceof HttpServletRequest) {
                HttpServletRequest httpServletRequest = (HttpServletRequest) request;

                // 1. HTTP 헤더에서 traceId를 가져옵니다.
                String traceId = httpServletRequest.getHeader(TRACE_ID_HEADER);

                // 2. 헤더에 traceId가 없으면 새로 생성합니다. (요청하신 "현재시간의 timekey" 대신 UUID 권장)
                if (!StringUtils.hasText(traceId)) {
                    traceId = UUID.randomUUID().toString();
                    log.debug("New traceId generated for web request: {}", traceId);
                }

                // 3. MDC에 traceId를 설정합니다.
                MDC.put(MDC_KEY, traceId);
            }

            log.info("business logic start");

            // 4. 다음 필터 또는 서블릿(컨트롤러)을 실행합니다.
            chain.doFilter(request, response);

        } finally {
            log.info("business logic end");
            // 5. (매우 중요) 요청 처리가 끝나면 반드시 MDC를 비워줍니다.
            //    스레드 풀의 다른 스레드로 오염되는 것을 방지합니다.
            MDC.clear();
        }
    }

    // init, destroy 메서드는 기본 구현을 사용하므로 생략 가능합니다.
}