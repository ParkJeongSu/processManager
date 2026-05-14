package kr.co.aim.api.web.controller;


import kr.co.aim.api.service.PurgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/purge")
@RequiredArgsConstructor
public class PurgeController {

    private final PurgeService purgeService;

    @PostMapping("")
    public ResponseEntity<String> purge() {
        purgeService.purge();
        // 2. 성공 응답 (200 OK + JSON Data)
        return ResponseEntity.ok("SUCCESS");
    }
}
