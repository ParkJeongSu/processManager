package kr.co.aim.api.web.controller;

import kr.co.aim.api.dto.*;
import kr.co.aim.api.service.ProcessStatusService;
import kr.co.aim.common.condition.ProcessStatusHistoryCondition;
import kr.co.aim.domain.model.ProcessStatusHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/process-status")
@RequiredArgsConstructor
public class ProcessStatusController {

    private final ProcessStatusService processStatusService;

    @GetMapping("/history")
    public ResponseEntity<Page<ProcessStatusHistory>> getHistory(
            ProcessStatusHistoryCondition condition,
            Pageable pageable
    ) {
        Page<ProcessStatusHistory> processStatusHistoryPage = processStatusService.findProcessStatusHistoryWithConditions(condition,pageable);
        return ResponseEntity.ok(processStatusHistoryPage);
    }


}
