package kr.co.aim.api.schedule;

import kr.co.aim.api.service.ConnectionCheckService;
import kr.co.aim.api.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
@RequiredArgsConstructor
public class CheckMantiConnectionScheduler {

    private final ConnectionCheckService connectionCheckService;

    @Scheduled(fixedDelay = 60000) // 5초마다 실행
    @SchedulerLock(name = "checkMantiStatus",
            lockAtMostFor = "PT2M",     // 작업 최장 소요시간 + 버퍼
            lockAtLeastFor = "PT30S")    // 최소 간격(선택)
    public void checkMantiStatus() {
        log.info("⏰ checkMantiStatus start");
        connectionCheckService.checkMantiConnection();
        log.info("⏰ checkMantiStatus end");
    }

}