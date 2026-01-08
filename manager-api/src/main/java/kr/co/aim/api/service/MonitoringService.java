package kr.co.aim.api.service;


import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.transaction.annotation.Transactional;
import kr.co.aim.common.dto.ChartPointDto;
import kr.co.aim.common.dto.DashboardDataDto;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MonitoringService {

    @PersistenceContext
    private EntityManager em;

    // --- 메모리 저장소 (동시성 처리를 위해 synchronizedList 사용) ---
    private final List<ChartPointDto> cpuHistory = Collections.synchronizedList(new LinkedList<>());
    private final List<ChartPointDto> sessionHistory = Collections.synchronizedList(new LinkedList<>());
    private final List<ChartPointDto> lockHistory = Collections.synchronizedList(new LinkedList<>());

    private final int MAX_HISTORY_SIZE = 200;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    // --- SQL 쿼리 모음 (상수) ---
    // 1. 세션 수
    private static final String SQL_SESSION_COUNT =
            "SELECT COUNT(session_id) FROM sys.dm_exec_sessions WHERE is_user_process = 1";

    // 2. Lock 블로킹 수
    private static final String SQL_LOCK_COUNT =
            "SELECT COUNT(*) FROM sys.dm_exec_requests WHERE blocking_session_id <> 0";

    // 3. CPU 사용률 (MSSQL 전용 XML 파싱 쿼리 단순화 버전)
    // 실제로는 아래 쿼리가 복잡하므로, 가장 최신 1건만 가져오는 구조
    private static final String SQL_CPU_USAGE =
            "SELECT TOP 1 " +
                    "record.value('(./Record/SchedulerMonitorEvent/SystemHealth/ProcessUtilization)[1]', 'int') " +
                    "FROM (SELECT TOP 1 CONVERT(xml, record) AS [record] " +
                    "      FROM sys.dm_os_ring_buffers " +
                    "      WHERE ring_buffer_type = N'RING_BUFFER_SCHEDULER_MONITOR' " +
                    "      ORDER BY timestamp DESC) AS x";

    // 4. Log 사용률
    private static final String SQL_LOG_USAGE =
            "SELECT (total_log_size_in_bytes - used_log_space_in_bytes) * 100.0 / total_log_size_in_bytes " +
                    "FROM sys.dm_db_log_space_usage";

    // 5. TempDB 사용률 (여유 공간 비율이 나오므로 100에서 빼야 사용률임에 주의)
    private static final String SQL_TEMPDB_USAGE =
            "SELECT (SUM(unallocated_extent_page_count) * 1.0 / SUM(total_page_count)) * 100.0 " +
                    "FROM tempdb.sys.dm_db_file_space_usage";

    // 6. 최장 쿼리 시간 (초 단위)
    private static final String SQL_MAX_QUERY_TIME =
            "SELECT ISNULL(MAX(total_elapsed_time / 1000), 0) FROM sys.dm_exec_requests " +
                    "WHERE session_id > 50 AND status NOT IN ('background', 'sleeping')";

    /**
     * [스케줄러] 1분마다 실행되어 이력을 저장합니다.
     */
    @Scheduled(fixedRate = 60000) // 1분
    @Transactional(readOnly = true)
    public void recordHistory() {
        String currentTime = LocalDateTime.now().format(timeFormatter);

        // 1. 값 조회
        double cpu = executeNumericQuery(SQL_CPU_USAGE);
        double sessions = executeNumericQuery(SQL_SESSION_COUNT);
        double locks = executeNumericQuery(SQL_LOCK_COUNT);

        // 2. 리스트에 추가 및 크기 제한 (200개)
        addAndTrim(cpuHistory, new ChartPointDto(currentTime, cpu));
        addAndTrim(sessionHistory, new ChartPointDto(currentTime, sessions));
        addAndTrim(lockHistory, new ChartPointDto(currentTime, locks));

        System.out.println("[Monitoring] Data recorded at " + currentTime);
    }

    /**
     * [API용] 현재 상태 + 차트 이력 데이터를 모두 반환
     */
    @Transactional(readOnly = true)
    public DashboardDataDto getDashboardData() {
        DashboardDataDto data = new DashboardDataDto();
        Map<String, Object> currentStatus = new HashMap<>();

        // 1. 현재 상태 스냅샷 (상단 카드용)
        currentStatus.put("isConnected", checkConnection());
        currentStatus.put("sessionCount", (int) executeNumericQuery(SQL_SESSION_COUNT));
        currentStatus.put("lockCount", (int) executeNumericQuery(SQL_LOCK_COUNT));
        currentStatus.put("cpuUsage", executeNumericQuery(SQL_CPU_USAGE));

        // Log는 여유공간% 쿼리이므로 100 - 값 = 사용량
        double logFree = executeNumericQuery(SQL_LOG_USAGE);
        currentStatus.put("logUsage", 100.0 - logFree);

        // TempDB도 여유공간% -> 사용량 변환
        double tempFree = executeNumericQuery(SQL_TEMPDB_USAGE);
        currentStatus.put("tempDbUsage", 100.0 - tempFree);

        currentStatus.put("maxQueryTime", (int) executeNumericQuery(SQL_MAX_QUERY_TIME));
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

    // JPA Native Query 실행 및 숫자 반환용 공통 메서드
    private double executeNumericQuery(String sql) {
        try {
            Query query = em.createNativeQuery(sql);
            Object result = query.getSingleResult();

            if (result == null) return 0.0;
            return ((Number) result).doubleValue();
        } catch (Exception e) {
            // DB 연결 실패 혹은 쿼리 오류 시 0 반환 혹은 로그 출력
            System.err.println("Query Error: " + e.getMessage());
            return 0.0;
        }
    }

    // DB 연결 체크 (간단히 1을 조회)
    private boolean checkConnection() {
        try {
            em.createNativeQuery("SELECT 1").getSingleResult();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
