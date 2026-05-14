package kr.co.aim.api.schedule;

import kr.co.aim.api.service.PurgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
@RequiredArgsConstructor
public class PurgeScheduler {

    private final PurgeService purgeService;

    @Scheduled(fixedDelay = 5000) // 5초마다 실행
    //(초, 분, 시, 일, 월, 요일) 순서
    //@Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시에 실행
    //@Scheduled(cron = "${schedule.purge.cron}") // application.yml 에 있는 걸로 수행
    @SchedulerLock(name = "runPurge",
            lockAtMostFor = "PT10M",     // 작업 최장 소요시간 + 버퍼
            lockAtLeastFor = "PT1M")    // 최소 간격(선택)
    public void runPurge() {
        log.info("⏰ runPurge start");
        purgeService.purge();
        log.info("⏰ runPurge end");
    }

}