package kr.co.aim.api.web.controller;


import kr.co.aim.api.service.MNGInstanceService;
import kr.co.aim.common.dto.ProcessStatusResponseDto;
import kr.co.aim.common.dto.ProcessControlRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mng/instances")
@RequiredArgsConstructor
public class MNGInstanceController {

    private final MNGInstanceService MNGInstanceService;

    @GetMapping("/list")
    public List<ProcessStatusResponseDto> getList() {
        return MNGInstanceService.getProcessList();
    }

    @PostMapping("/{port}/start")
    public ResponseEntity<ProcessStatusResponseDto> start(@PathVariable int port,@RequestBody ProcessControlRequestDto requestDto) {
        // 1. 서비스 로직 호출 (예외 발생 시 GlobalExceptionHandler로 자동 위임됨)
        ProcessStatusResponseDto result = MNGInstanceService.startProcess(port,requestDto);
        // 2. 성공 응답 (200 OK + JSON Data)
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{port}/stop")
    public ResponseEntity<ProcessStatusResponseDto> stop(@PathVariable int port,@RequestBody ProcessControlRequestDto requestDto) {
        ProcessStatusResponseDto result = MNGInstanceService.stopProcess(port,requestDto);
        return ResponseEntity.ok(result);
    }
}
