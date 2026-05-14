package kr.co.aim.infra.persistence.springdatajpa;

import kr.co.aim.infra.persistence.entity.DualEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MonitoringJpaRepository extends JpaRepository<DualEntity, Long> { // 임의의 엔티티 설정

    @Query(value = "SELECT COUNT(session_id) FROM sys.dm_exec_sessions WHERE is_user_process = 1", nativeQuery = true)
    Long getSessionCount();

    @Query(value = "SELECT COUNT(*) FROM sys.dm_exec_requests WHERE blocking_session_id <> 0", nativeQuery = true)
    Long getLockCount();

    @Query(value = "SELECT TOP 1 record.value('(./Record/SchedulerMonitorEvent/SystemHealth/ProcessUtilization)[1]', 'int') " +
            "FROM (SELECT TOP 1 CONVERT(xml, record) AS [record] FROM sys.dm_os_ring_buffers " +
            "WHERE ring_buffer_type = N'RING_BUFFER_SCHEDULER_MONITOR' ORDER BY timestamp DESC) AS x", nativeQuery = true)
    Integer getCpuUsage();

    @Query(value = "SELECT (total_log_size_in_bytes - used_log_space_in_bytes) * 100.0 / total_log_size_in_bytes FROM sys.dm_db_log_space_usage", nativeQuery = true)
    Double getLogUsagePercentage();

    @Query(value = "SELECT (SUM(unallocated_extent_page_count) * 1.0 / SUM(total_page_count)) * 100.0 FROM tempdb.sys.dm_db_file_space_usage", nativeQuery = true)
    Double getTempDbFreePercentage();

    @Query(value = "SELECT ISNULL(MAX(total_elapsed_time / 1000), 0) FROM sys.dm_exec_requests WHERE session_id > 50 AND status NOT IN ('background', 'sleeping')", nativeQuery = true)
    Integer getMaxQueryTime();

    @Query(value = "SELECT 1", nativeQuery = true)
    Integer checkConnection();
}