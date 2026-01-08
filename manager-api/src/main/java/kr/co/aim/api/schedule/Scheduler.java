package kr.co.aim.api.schedule;

import kr.co.aim.api.service.MNGInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class Scheduler {

    private final MNGInstanceService mngInstanceService;

    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    @SchedulerLock(name = "checkProcessStatus",
            lockAtMostFor = "PT2M",     // 작업 최장 소요시간 + 버퍼
            lockAtLeastFor = "PT30S")    // 최소 간격(선택)
    public void checkProcessStatus() {
        log.info("⏰ checkProcessStatus start");
        mngInstanceService.checkProcessStatus();
        log.info("⏰ checkProcessStatus end");
    }

}