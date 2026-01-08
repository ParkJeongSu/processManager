package kr.co.aim.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardDataDto {
    // 현재 상태 (상단 카드용)
    private Map<String, Object> currentStatus;
    // 과거 이력 (하단 차트용)
    private List<ChartPointDto> cpuHistory;
    private List<ChartPointDto> sessionHistory;
    private List<ChartPointDto> lockHistory;
}
