package kr.co.aim.api.service;


import kr.co.aim.common.condition.ProcessControlRequestCondition;
import org.springframework.scheduling.annotation.Async;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessAsyncService {

    private final RestTemplate restTemplate;
    private final ProcessStatusService processStatusService;

    // 비동기로 실제 종료 명령을 수행하는 메서드
    @Async
    public void performShutdown(int port, String url, ProcessControlRequestCondition requestVo) {
        try {
            log.info("비동기 종료 요청 시작: {}", url);
            // 실제 종료 명령 전송
            restTemplate.postForObject(url, null, String.class);

            // 정상 응답이 온 경우
            processStatusService.markAsDown(port, requestVo);
            log.info("프로세스가 정상적으로 종료 응답을 보냈습니다. (Port: {})", port);

        } catch (ResourceAccessException e) {
            // 타임아웃(SocketTimeoutException)이 이쪽으로 떨어집니다.
            log.warn("종료 응답을 받지 못했으나(Timeout), 프로세스가 종료 중인 것으로 간주합니다. (Port: {})", port);

            // 응답은 못 받았지만, 실제로 죽었는지는 나중에 스케줄러가 확인할 것이므로
            // 일단 DB 상태를 DOWN 혹은 적절한 상태로 변경합니다.
            processStatusService.markAsDown(port, requestVo);

        } catch (Exception e) {
            log.error("종료 처리 중 예상치 못한 오류 발생 (Port: {})", port, e);
        }
    }

}
