package kr.co.aim.api.web.controller;


import kr.co.aim.api.service.ProcessService;
import kr.co.aim.api.dto.ProcessStatusResponseDto;
import kr.co.aim.common.condition.ProcessControlRequestCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService ProcessService;

    @GetMapping("/list")
    public ResponseEntity<Page<ProcessStatusResponseDto>> getList() {
        List<ProcessStatusResponseDto> responseDtoList = ProcessService.getProcessList();
        return ResponseEntity.ok(new PageImpl<>(responseDtoList));
    }

    @PostMapping("/{port}/start")
    public ResponseEntity<Page<ProcessStatusResponseDto>> start(@PathVariable int port,@RequestBody ProcessControlRequestCondition requestCondition) {
        // 1. 서비스 로직 호출 (예외 발생 시 GlobalExceptionHandler로 자동 위임됨)
        ProcessStatusResponseDto resultDto = ProcessService.startProcess(port,requestCondition);
        List<ProcessStatusResponseDto> content = Collections.singletonList(resultDto);
        // 2. 성공 응답 (200 OK + JSON Data)
        return ResponseEntity.ok(new PageImpl<>(content));
    }

    @PostMapping("/{port}/stop")
    public ResponseEntity<Page<ProcessStatusResponseDto>> stop(@PathVariable int port,@RequestBody ProcessControlRequestCondition requestCondition) {
        ProcessStatusResponseDto resultDto = ProcessService.stopProcess(port,requestCondition);
        List<ProcessStatusResponseDto> content = Collections.singletonList(resultDto);
        return ResponseEntity.ok(new PageImpl<>(content));
    }
}
