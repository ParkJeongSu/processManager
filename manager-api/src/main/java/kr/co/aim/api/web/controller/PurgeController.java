package kr.co.aim.api.web.controller;


import kr.co.aim.api.dto.PurgeConfigCreateDto;
import kr.co.aim.api.dto.PurgeConfigUpdateDto;
import kr.co.aim.api.service.PurgeConfigService;
import kr.co.aim.api.service.PurgeLogService;
import kr.co.aim.api.service.PurgeService;
import kr.co.aim.common.condition.PurgeConfigSearchCondition;
import kr.co.aim.common.condition.PurgeLogSearchCondition;
import kr.co.aim.domain.model.PurgeConfig;
import kr.co.aim.domain.model.PurgeLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/purge")
@RequiredArgsConstructor
public class PurgeController {
    private final PurgeService purgeService;
    private final PurgeConfigService purgeConfigService;
    private final PurgeLogService purgeLogService;

    @PostMapping("/execute")
    public ResponseEntity<String> purge() {
        purgeService.purge();
        // 2. 성공 응답 (200 OK + JSON Data)
        return ResponseEntity.ok("SUCCESS");
    }

    @PostMapping("")
    public ResponseEntity<Page<PurgeConfig>> createPurgeConfig(
            @RequestBody PurgeConfigCreateDto dto
    ) {
        PurgeConfig purgeConfig = purgeConfigService.create(dto);

        List<PurgeConfig> purgeConfigList = Collections.singletonList(purgeConfig);
        // 2. 성공 응답 (200 OK + JSON Data)
        return ResponseEntity.ok(new PageImpl<>(purgeConfigList,Pageable.unpaged(),purgeConfigList.size()));
    }

    // 1. 요청 접수: PATCH /api/process-info/{port}
    @PatchMapping("/{id}")
    public ResponseEntity<Page<PurgeConfig>> changePurgeConfig(
            @PathVariable("id") Integer id,
            @RequestBody PurgeConfigUpdateDto dto
    ) {
        dto.setId(id);

        // 3. 서비스 계층에 작업 위임
        PurgeConfig purgeConfig = purgeConfigService.update(dto);

        List<PurgeConfig> content = Collections.singletonList(purgeConfig);
        return ResponseEntity.ok(new PageImpl<>(content));
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePurgeConfigList(@RequestBody kr.co.aim.common.condition.DeleteIntegerItemListDto request) {
        purgeConfigService.deleteAllByIdInBatch(request.getIds());
        // 성공적으로 삭제되었으며, 별도의 본문 내용 없이 응답한다는 의미
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    public ResponseEntity<Page<PurgeConfig>> getPurgeConfigList(
            PurgeConfigSearchCondition condition,
            Pageable pageable) {
        // TODO: PurgeConfigSearchCondition 을 사용해서 PurgeConfigList 가져오기
        List<PurgeConfig> purgeConfigList = purgeConfigService.findAll();
        // 4. 결과 변환 및 HTTP 응답
        return ResponseEntity.ok(new PageImpl<>(purgeConfigList, pageable, purgeConfigList.size()));
    }

    @GetMapping("/history")
    public ResponseEntity<Page<PurgeLog>> getPurgeLogList(
            PurgeLogSearchCondition condition,
            Pageable pageable) {
        // 3. 서비스 계층에 작업 위임
        Page<PurgeLog> purgeConfigList = purgeLogService.findPurgeLogWithConditions(condition,pageable);
        // 4. 결과 변환 및 HTTP 응답
        return ResponseEntity.ok(purgeConfigList);
    }
}
