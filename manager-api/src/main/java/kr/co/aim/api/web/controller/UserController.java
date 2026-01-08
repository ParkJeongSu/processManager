package kr.co.aim.api.web.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import kr.co.aim.api.service.ExcelService;
import kr.co.aim.common.dto.*;
import kr.co.aim.common.error.ExcelValidationException;
import kr.co.aim.domain.model.User;
import kr.co.aim.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService; // 👈 Domain 계층의 Service에 의존
    private final ExcelService excelService; // 1. ExcelService 주입

    // 1. 요청 접수: PATCH /api/users/{user-id}
    @PatchMapping("/{user-id}")
    public ResponseEntity<UserResponseDto> changeUser(
            @PathVariable("user-id") Long userId,
            @RequestBody UserUpdateRequestDto requestDto
    ) {
        // 3. 서비스 계층에 작업 위임
        User updatedUser = userService.changeUser(userId,requestDto);

        // 4. 결과 변환 및 HTTP 응답
        UserResponseDto responseDto =
                UserResponseDto.builder()
                        .id(updatedUser.getId())
                        .authorityId(updatedUser.getAuthorityId())
                        .userId(updatedUser.getUserId())
                        .email(updatedUser.getEmail())
                        .userName(updatedUser.getUserName())
                        .phone1(updatedUser.getPhone1())
                        .phone2(updatedUser.getPhone2())
                        .build();
        
        return ResponseEntity.ok(responseDto);
    }

    // 1. 요청 접수: PATCH /api/users
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserCreateRequestDto requestDto) {
        // 3. 서비스 계층에 작업 위임
        User updatedUser = userService.createUser(requestDto);

        // 4. 결과 변환 및 HTTP 응답
        UserResponseDto responseDto =
                UserResponseDto.builder()
                        .id(updatedUser.getId())
                        .authorityId(updatedUser.getAuthorityId())
                        .userId(updatedUser.getUserId())
                        .email(updatedUser.getEmail())
                        .userName(updatedUser.getUserName())
                        .phone1(updatedUser.getPhone1())
                        .phone2(updatedUser.getPhone2())
                        .build();

        return ResponseEntity.ok(responseDto);
    }

    // 1. 요청 접수: GET /api/users/
    @GetMapping
    public ResponseEntity<Page<UserResponseDto>> getUsers(
            UserSearchConditionDto condition,
            Pageable pageable) {
        // 3. 서비스 계층에 작업 위임
        //Page<UserResponseDto> userPage = userService.findUsers(pageable);
        Page<UserResponseDto> userPage = userService.findUsers(condition, pageable);

        // 4. 결과 변환 및 HTTP 응답
        return ResponseEntity.ok(userPage);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteUsers(@RequestBody DeleteItemListDto request) {
        userService.deleteUsersByIds(request.getIds());
        // 성공적으로 삭제되었으며, 별도의 본문 내용 없이 응답한다는 의미
        return ResponseEntity.noContent().build();
    }

    // 1. 요청 접수: GET /api/users/
    @GetMapping("/export")
    public void getAllUsers(
            HttpServletResponse response, // 엑셀 파일을 스트리밍하기 위해 필요
            UserSearchConditionDto condition,
            Pageable pageable) {
        // 3. 서비스 계층에 작업 위임
        Page<UserResponseDto> page = userService.findUsers(condition, Pageable.unpaged());

        // 3. Page에서 실제 데이터 리스트(List)를 가져옴
        List<UserResponseDto> dataList = page.getContent();

        // 4. ExcelService에 쓰기 작업 위임 (헤더 설정 및 파일 생성)
        excelService.writeToExcel(response ,dataList);
    }

    // 1. 요청 접수: PATCH /api/users
    @PostMapping("/import")
    public ResponseEntity<?> importUsers(@RequestParam("file") MultipartFile file) {
        try {
            // 서비스 로직에서 (2)~(5) 검증 수행
            List<UserCreateRequestDto> importList = excelService.importData(file,UserCreateRequestDto.class);

            // importList 를 가지고
            userService.createUsers(importList);

            // 200 OK (성공)
            return ResponseEntity.ok().build();

        } catch (ExcelValidationException ex) {
            // (2)~(5) 검증 실패 시
            // ex.getErrors()는 ["3행: ...", "5행: ..."] 같은 List<String>

            // 400 Bad Request와 오류 메시지 목록 반환
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Validation failed");
            errorResponse.put("errors", ex.getErrorMessages());

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            // 그 외 서버 내부 오류
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "An internal server error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



}
