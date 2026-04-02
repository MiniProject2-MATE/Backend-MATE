package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.AuthResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface AuthService {
    // 1. 회원가입
    UserResponseDto register(UserRequestDto requestDto, MultipartFile profileImage);

    // 2. 중복 확인 기능들 (가입 전 필수!)
    boolean checkEmailDuplicate(String email);
    boolean checkNicknameDuplicate(String nickname);
    boolean checkPhoneDuplicate(String phoneNumber);

    // 3. 찾기 기능들
    String findEmailByPhoneNumber(String phoneNumber);
    void resetPassword(String email, String phoneNumber);

    // 4. 로그인
    AuthResponseDto login(String email, String password);
}