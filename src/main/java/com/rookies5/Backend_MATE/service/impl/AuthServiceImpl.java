package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.AuthResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.security.JwtTokenProvider;
import com.rookies5.Backend_MATE.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 1. 신규 회원을 등록(회원가입)합니다.
     */
    @Override
    public UserResponseDto register(UserRequestDto requestDto, MultipartFile profileImage) {
        // 이메일 및 전화번호 중복 검증
        if (checkEmailDuplicate(requestDto.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        if (checkPhoneDuplicate(requestDto.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        }

        // 비밀번호 암호화 적용 (BCrypt)
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(encodedPassword);

        // 이미지 파일 처리 (S3 연동 전 임시 로직)
        if (profileImage != null && !profileImage.isEmpty()) {
            String profileImgUrl = "https://mate-s3.com/uploaded-" + profileImage.getOriginalFilename();
            requestDto.setProfileImg(profileImgUrl);
        }

        // Mapper를 통해 Entity 변환
        User user = UserMapper.mapToUser(requestDto);

        // 최종 닉네임 중복 검증
        if (checkNicknameDuplicate(user.getNickname())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        User savedUser = userRepository.save(user);
        return UserMapper.mapToUserResponse(savedUser);
    }

    /**
     * 2. 로그인 및 토큰 발급 (JWT 통합)
     * API 명세서 v1.1 규격을 준수합니다.
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(String email, String password) {
        try {
            // 1. 인증 객체 생성 및 검증 (CustomUserDetailsService 호출)
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, password);

            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            // 2. 인증 성공 시 토큰 생성 (Access 1시간, Refresh 7일)
            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            // 3. 응답에 포함할 유저 정보 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            // 4. 설계서에 정의된 AuthResponseDto 반환
            return AuthResponseDto.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(3600)
                    .user(AuthResponseDto.UserInfo.builder()
                            .id(user.getId())
                            .nickname(user.getNickname())
                            .email(user.getEmail())
                            .position(user.getPosition() != null ? user.getPosition().name() : null)
                            .build())
                    .build();

        } catch (AuthenticationException e) {
            // 이메일/비밀번호 불일치 시 AUTH_001 에러 발생
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    // 이메일 중복 확인
    @Transactional(readOnly = true)
    @Override
    public boolean checkEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // 닉네임 중복 확인
    @Transactional(readOnly = true)
    @Override
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    // 전화번호 중복 확인
    @Transactional(readOnly = true)
    @Override
    public boolean checkPhoneDuplicate(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    // 아이디(이메일) 찾기
    @Transactional(readOnly = true)
    @Override
    public String findEmailByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, phoneNumber));
        return user.getEmail();
    }

    // 비밀번호 찾기 (임시 비밀번호 발급)
    @Override
    public void resetPassword(String email, String phoneNumber) {
        User user = userRepository.findByEmailAndPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_MATCHED));

        String tempPassword = generateTempPassword();

        // 💡 임시 비밀번호 역시 암호화하여 DB에 저장해야 합니다.
        user.updatePassword(passwordEncoder.encode(tempPassword));

        // TODO: 실제 서비스 시 emailService.sendTempPassword(email, tempPassword) 호출 필요
    }

    /**
     * 내부 유틸 메서드: 8자리 임시 비밀번호 생성
     */
    private String generateTempPassword() {
        char[] charSet = new char[]{
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
                'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
                'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
        };
        StringBuilder tempPw = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int idx = (int) (charSet.length * Math.random());
            tempPw.append(charSet[idx]);
        }
        return tempPw.toString();
    }
}