package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService; // AuthServiceImpl이 주입됩니다.

    /**
     * 1. 회원가입 (Signup)
     * MultipartFile 처리를 위해 RequestPart를 사용하거나 param으로 받습니다.
     */
    @PostMapping("/signup")
    public ResponseEntity<UserResponseDto> signup(
            @RequestPart("userData") UserRequestDto requestDto,
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
     */
    @GetMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestParam String nickname) {
        return ResponseEntity.ok(authService.checkNicknameDuplicate(nickname));
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