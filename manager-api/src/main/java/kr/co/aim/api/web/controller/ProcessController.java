package kr.co.aim.api.web.controller;


import kr.co.aim.api.service.ProcessService;
import kr.co.aim.api.dto.ProcessStatusResponseDto;
import kr.co.aim.api.dto.ProcessControlRequestDto;
import kr.co.aim.common.vo.ProcessControlRequestVo;
import kr.co.aim.common.vo.ProcessStatusResponseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService ProcessService;

    @GetMapping("/list")
    public List<ProcessStatusResponseDto> getList() {
        return ProcessService.getProcessList();
    }

    @PostMapping("/{port}/start")
    public ResponseEntity<ProcessStatusResponseDto> start(@PathVariable int port,@RequestBody ProcessControlRequestDto requestDto) {
        // 1. 서비스 로직 호출 (예외 발생 시 GlobalExceptionHandler로 자동 위임됨)
        ProcessControlRequestVo vo = ProcessControlRequestDto.toVo(requestDto);
        ProcessStatusResponseVo resultVo = ProcessService.startProcess(port,vo);
        ProcessStatusResponseDto resultDto = ProcessStatusResponseDto.from(resultVo);
        // 2. 성공 응답 (200 OK + JSON Data)
        return ResponseEntity.ok(resultDto);
    }

    @PostMapping("/{port}/stop")
    public ResponseEntity<ProcessStatusResponseDto> stop(@PathVariable int port,@RequestBody ProcessControlRequestDto requestDto) {
        ProcessControlRequestVo vo = ProcessControlRequestDto.toVo(requestDto);
        ProcessStatusResponseVo resultVo = ProcessService.stopProcess(port,vo);
        ProcessStatusResponseDto resultDto = ProcessStatusResponseDto.from(resultVo);
        return ResponseEntity.ok(resultDto);
    }
}
