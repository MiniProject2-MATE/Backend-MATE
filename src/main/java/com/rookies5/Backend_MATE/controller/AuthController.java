package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
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

    private final AuthService authService; // AuthServiceImpl이 주입됩니다.

    /**
     * 1. 회원가입 (Signup)
     * MultipartFile 처리를 위해 RequestPart를 사용하거나 param으로 받습니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(
            @Valid @RequestPart("userData") UserRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        return ResponseEntity.ok(authService.register(requestDto, profileImage));
    }

    /**
     * 2. 이메일 중복 확인
     */
    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.checkEmailDuplicate(email));
    }

    /**
     * 3. 닉네임 중복 확인
     * GET /api/auth/check-nickname?nickname=개발왕&userId=1
     */
    @GetMapping("/check-nickname")
    public SuccessResponse<Map<String, Boolean>> checkNickname(
            @RequestParam String nickname,
            @RequestParam(required = false) Long userId) { // 👈 userId를 선택적으로 받게 추가!

        log.info("닉네임 중복 확인 요청: {} (userId: {})", nickname, userId);

        // 1. 서비스 호출 (기존 checkNicknameDuplicate 대신 우리가 만든 isNicknameAvailable 추천)
        // 만약 서비스 메서드명을 안 바꿨다면 파라미터에 userId만 추가해서 호출하세요.
        boolean isAvailable = authService.isNicknameAvailable(nickname, userId);

        // 2. 데이터 구성
        Map<String, Boolean> data = new HashMap<>();
        data.put("isAvailable", isAvailable);

        // 3. 중복이거나 형식이 틀린 경우 서비스(isNicknameAvailable)에서 이미 Exception을 던지므로
        // 컨트롤러에서는 깔끔하게 리턴만 하면 됩니다!

        return new SuccessResponse<>("사용 가능한 닉네임입니다.", data);
    }

    /**
     * 4. 아이디(이메일) 찾기
     */
    @GetMapping("/find-email")
    public ResponseEntity<String> findEmail(@RequestParam String phoneNumber) {
        return ResponseEntity.ok(authService.findEmailByPhoneNumber(phoneNumber));
    }

    /**
     * 5. 비밀번호 재설정 (임시 비번 발급)
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String phoneNumber) {
        // 서비스에서 생성된 임시 비번을 받음
        String newPassword = authService.resetPassword(email, phoneNumber);

        // 200 OK와 함께 바디에 비번을 실어서 보냄
        return ResponseEntity.ok(newPassword);
    }
}