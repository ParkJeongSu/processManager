package kr.co.aim.api.schedule;

import kr.co.aim.api.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//@Component
@Slf4j
@RequiredArgsConstructor
public class RecordMonitoringScheduler {

    private final MonitoringService monitoringService;

    @Scheduled(fixedDelay = 60000) // 6초마다 실행
    @SchedulerLock(name = "recordMonitoring",
            lockAtMostFor = "PT2M",     // 작업 최장 소요시간 + 버퍼
            lockAtLeastFor = "PT30S")    // 최소 간격(선택)
    public void recordMonitoring() {
        log.info("⏰ recordMonitoring start");
        monitoringService.recordHistory();
        log.info("⏰ recordMonitoring end");
    }

}