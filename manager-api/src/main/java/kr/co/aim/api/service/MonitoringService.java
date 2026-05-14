package kr.co.aim.api.service;


import kr.co.aim.domain.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import kr.co.aim.api.dto.ChartPointDto;
import kr.co.aim.api.dto.DashboardDataDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MonitoringService {

    private final MonitoringRepository monitoringRepository;

    // --- 메모리 저장소 (동시성 처리를 위해 synchronizedList 사용) ---
    private final List<ChartPointDto> cpuHistory = Collections.synchronizedList(new LinkedList<>());
    private final List<ChartPointDto> sessionHistory = Collections.synchronizedList(new LinkedList<>());
    private final List<ChartPointDto> lockHistory = Collections.synchronizedList(new LinkedList<>());
    private final int MAX_HISTORY_SIZE = 200;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");


    /**
     * 1분마다 실행되어 이력을 저장합니다.
     */
    @Transactional
    public void recordHistory() {
        String currentTime = LocalDateTime.now().format(timeFormatter);

        // 1. 값 조회
        Integer cpuUsage = monitoringRepository.getCpuUsage();
        Long sessionCount = monitoringRepository.getSessionCount();
        Long lockCount = monitoringRepository.getLockCount();

        double cpu = ObjectUtils.isEmpty(cpuUsage) ? -1 : cpuUsage;
        double sessions = ObjectUtils.isEmpty(sessionCount) ? -1 : sessionCount;
        double locks = ObjectUtils.isEmpty(lockCount) ? -1 : lockCount;

        // 2. 리스트에 추가 및 크기 제한 (200개)
        addAndTrim(cpuHistory, new ChartPointDto(currentTime, cpu));
        addAndTrim(sessionHistory, new ChartPointDto(currentTime, sessions));
        addAndTrim(lockHistory, new ChartPointDto(currentTime, locks));

        log.info("[Monitoring] Data recorded at " + currentTime);
    }

    /**
     * [API용] 현재 상태 + 차트 이력 데이터를 모두 반환
     */
    @Transactional(readOnly = true)
    public DashboardDataDto getDashboardData() {
        DashboardDataDto data = new DashboardDataDto();
        Map<String, Object> currentStatus = new HashMap<>();

        // 1. 값 조회
        boolean isConnected = checkConnection();
        Integer cpuUsage = monitoringRepository.getCpuUsage();
        Long sessionCount = monitoringRepository.getSessionCount();
        Long lockCount = monitoringRepository.getLockCount();

        long sessions = ObjectUtils.isEmpty(sessionCount) ? -1 : sessionCount;
        long locks = ObjectUtils.isEmpty(lockCount) ? -1 : lockCount;
        double cpu = ObjectUtils.isEmpty(cpuUsage) ? -1 : cpuUsage;

        // 1. 현재 상태 스냅샷 (상단 카드용)
        currentStatus.put("isConnected", isConnected);
        currentStatus.put("sessionCount", sessions);
        currentStatus.put("lockCount", locks);
        currentStatus.put("cpuUsage", cpu);

        // Log는 여유공간% 쿼리이므로 100 - 값 = 사용량
        double logFree = monitoringRepository.getLogUsagePercentage();
        currentStatus.put("logUsage", 100.0 - logFree);

        // TempDB도 여유공간% -> 사용량 변환
        double tempFree = monitoringRepository.getTempDbFreePercentage();
        currentStatus.put("tempDbUsage", 100.0 - tempFree);
        Integer maxQueryTimeData = monitoringRepository.getMaxQueryTime();
        int maxQueryTime = ObjectUtils.isEmpty(maxQueryTimeData) ? -1 : maxQueryTimeData;
        currentStatus.put("maxQueryTime", maxQueryTime);
        // Disk 용량은 복잡해서 일단 제외 (필요시 추가)

        data.setCurrentStatus(currentStatus);

        // 2. 차트 데이터 (메모리 리스트 복사해서 반환)
        // 동기화 블록을 사용하여 복사 중 수정 방지
        synchronized (cpuHistory) {
            data.setCpuHistory(new ArrayList<>(cpuHistory));
        }
        synchronized (sessionHistory) {
            data.setSessionHistory(new ArrayList<>(sessionHistory));
        }
        synchronized (lockHistory) {
            data.setLockHistory(new ArrayList<>(lockHistory));
        }

        return data;
    }

    // --- 내부 헬퍼 메서드 ---

    // 리스트에 추가하고 200개 넘으면 삭제하는 로직
    private void addAndTrim(List<ChartPointDto> list, ChartPointDto point) {
        synchronized (list) {
            list.add(point);
            if (list.size() > MAX_HISTORY_SIZE) {
                list.remove(0); // 가장 오래된 데이터 삭제
            }
        }
    }

    // DB 연결 체크 (간단히 1을 조회)
    private boolean checkConnection() {
        try {
            Integer connection = monitoringRepository.checkConnection();
            return connection.equals(1);
        } catch (Exception e) {
            return false;
        }
    }


}
