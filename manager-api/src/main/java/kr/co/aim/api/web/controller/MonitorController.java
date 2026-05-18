package kr.co.aim.api.web.controller;


import kr.co.aim.api.service.MonitoringService;
import kr.co.aim.api.dto.DashboardDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/monitoring")
@RequiredArgsConstructor
public class MonitorController {
    private final MonitoringService monitoringService;

    @GetMapping("/status")
    public Page<DashboardDataDto> getStatus() {

        DashboardDataDto data = monitoringService.getDashboardData();
        // 객체를 리스트로 감싸서 전달
        List<DashboardDataDto> content = Collections.singletonList(data);
        return new PageImpl<>(content,Pageable.unpaged(),content.size());
    }
}
