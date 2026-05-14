package kr.co.aim.domain.repository;

public interface MonitoringRepository{

    Long getSessionCount();

    Long getLockCount();

    Integer getCpuUsage();

    Double getLogUsagePercentage();

    Double getTempDbFreePercentage();

    Integer getMaxQueryTime();

    Integer checkConnection();
}