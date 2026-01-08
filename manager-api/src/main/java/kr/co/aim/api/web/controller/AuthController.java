package kr.co.aim.api.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.aim.api.service.UserService;
import kr.co.aim.common.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // TODO: 아직 회원가입 화면은 안 만들었음 추후 만들기
    @PostMapping("/join")
    public ResponseEntity<Long> join(@RequestBody MemberJoinRequestDto memberJoinRequestDto) {
        Long savedId = userService.join(memberJoinRequestDto);
        return new ResponseEntity<>(savedId, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody MemberLoginRequestDto memberLoginRequestDto) {
        TokenDto tokenDto = userService.login(memberLoginRequestDto);
        return new ResponseEntity<>(tokenDto, HttpStatus.OK);
    }

    @GetMapping("/test")
    public ResponseEntity<String> test() {
        // 현재 인증된 사용자의 정보를 가져올 수 있습니다.
        // 여기서는 간단히 성공 메시지만 반환합니다.
        return new ResponseEntity<>("인증 성공!", HttpStatus.OK);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenDto> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        TokenDto tokenDto = userService.reissue(tokenRequestDto);
        return new ResponseEntity<>(tokenDto, HttpStatus.OK);
    }

}
