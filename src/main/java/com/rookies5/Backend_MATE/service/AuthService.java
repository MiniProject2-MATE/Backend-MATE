package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.AuthResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    // 1. 회원가입
    UserResponseDto register(UserRequestDto requestDto, MultipartFile profileImage);

    // 2. 로그인
    AuthResponseDto login(String email, String password);

    // 3. 이메일 유효성 및 중복 확인
    boolean isEmailAvailable(String email);

    // 4. 전화번호 가용성 체크
    boolean isPhoneAvailable(String phoneNumber, Long userId);

    // 5. 닉네임 가용성 체크
    boolean isNicknameAvailable(String nickname, Long currentUserId);

    // 6. 아이디(이메일) 찾기
    String findEmailByPhoneNumber(String phoneNumber);

    // 7. 비밀번호 찾기 (임시 발급)
    String resetPassword(String email, String phoneNumber);
}