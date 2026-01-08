package kr.co.aim.api.web.controller;


import kr.co.aim.api.service.MonitoringService;
import kr.co.aim.common.dto.DashboardDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitorController {
    private final MonitoringService monitoringService;

    @GetMapping("/status")
    public DashboardDataDto getStatus() {
        return monitoringService.getDashboardData();
    }
}
