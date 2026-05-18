package kr.co.aim.api.schedule;

import kr.co.aim.api.service.ProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
@RequiredArgsConstructor
public class CheckProcessScheduler {

    private final ProcessService processService;

    @Scheduled(fixedDelay = 30000) // 5초마다 실행
    @SchedulerLock(name = "checkProcessStatus",
            lockAtMostFor = "PT2M",     // 작업 최장 소요시간 + 버퍼
            lockAtLeastFor = "PT30S")    // 최소 간격(선택)
    public void checkProcessStatus() {
        log.info("⏰ checkProcessStatus start");
        processService.checkProcessStatus();
        log.info("⏰ checkProcessStatus end");
    }

}