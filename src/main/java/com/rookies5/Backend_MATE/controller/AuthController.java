package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * 1. 회원가입 (Signup)
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(
            @Valid @RequestPart("userData") UserRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        log.info("회원가입 요청: {}", requestDto.getEmail());
        return ResponseEntity.ok(authService.register(requestDto, profileImage));
    }

    /**
     * 2. 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public SuccessResponse<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        log.info("이메일 중복 확인 요청: {}", email);

        // 서비스 내부에서 중복 시 USER_002, 형식 오류 시 AUTH_INVALID_CREDENTIALS 투척!
        boolean isAvailable = authService.isEmailAvailable(email);

        Map<String, Boolean> data = new HashMap<>();
        data.put("isAvailable", isAvailable);
        return new SuccessResponse<>("사용 가능한 이메일입니다.", data);
    }

    /**
     * 3. 닉네임 중복 확인
     */
    @GetMapping("/check-nickname")
    public SuccessResponse<Map<String, Boolean>> checkNickname(
            @RequestParam String nickname,
            @RequestParam(required = false) Long userId) {
        log.info("닉네임 중복 확인 요청: {} (userId: {})", nickname, userId);

        // 서비스 내부에서 중복 시 USER_003, 형식 오류 시 USER_007 투척!
        boolean isAvailable = authService.isNicknameAvailable(nickname, userId);

        Map<String, Boolean> data = new HashMap<>();
        data.put("isAvailable", isAvailable);
        return new SuccessResponse<>("사용 가능한 닉네임입니다.", data);
    }

    /**
     * 4. 전화번호 중복 확인
     * GET /api/auth/check-phone?phoneNumber=01011112222&userId=1
     */
    @GetMapping("/check-phone")
    public SuccessResponse<Map<String, Boolean>> checkPhone(
            @RequestParam String phoneNumber,
            @RequestParam(required = false) Long userId) {

        log.info("전화번호 중복 확인 요청: {} (userId: {})", phoneNumber, userId);

        // 서비스 호출 시 userId 전달
        boolean isAvailable = authService.isPhoneAvailable(phoneNumber, userId);

        Map<String, Boolean> data = new HashMap<>();
        data.put("isAvailable", isAvailable);

        return new SuccessResponse<>("사용 가능한 전화번호입니다.", data);
    }

    /**
     * 5. 아이디(이메일) 찾기
     */
    @GetMapping("/find-email")
    public ResponseEntity<String> findEmail(@RequestParam String phoneNumber) {
        return ResponseEntity.ok(authService.findEmailByPhoneNumber(phoneNumber));
    }

    /**
     * 6. 비밀번호 재설정 (임시 비번 발급)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String phoneNumber) {
        String newPassword = authService.resetPassword(email, phoneNumber);
        return ResponseEntity.ok(newPassword);
    }
}