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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    // Security 관련 의존성 추가
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    // ✨ 추가: 이미지가 저장될 실제 경로 (내 컴퓨터 사용자 폴더 하위)
    private final String uploadPath = System.getProperty("user.home") + "/mate_uploads/profiles/";

    /**
     * 1. 신규 회원을 등록(회원가입) - 이미지 로컬 저장 로직 적용
     */
    @Override
    public UserResponseDto register(UserRequestDto requestDto, MultipartFile profileImage) {
        // 중복 체크
        isEmailAvailable(requestDto.getEmail());
        isPhoneAvailable(requestDto.getPhoneNumber(), null);

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        requestDto.setPassword(encodedPassword);

        // ✨ 이미지 처리 로직 수정됨
        if (profileImage != null && !profileImage.isEmpty()) {
            try {
                // 1. 폴더 생성 (없으면 자동 생성)
                java.io.File folder = new java.io.File(uploadPath);
                if (!folder.exists()) folder.mkdirs();

                // 2. 파일명 생성 (UUID를 써서 겹치지 않게 함)
                String originalFilename = profileImage.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String storeFilename = java.util.UUID.randomUUID().toString() + extension;

                // 3. 내 컴퓨터 폴더에 실제 파일 저장
                profileImage.transferTo(new java.io.File(uploadPath + storeFilename));

                // 4. DTO에 저장된 상대 경로 세팅 (프론트엔드 접근용)
                requestDto.setProfileImg("/uploads/profiles/" + storeFilename);

                log.info("프로필 이미지 저장 성공: {}", storeFilename);
            } catch (java.io.IOException e) {
                log.error("파일 저장 중 오류 발생: {}", e.getMessage());
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR); // 이 에러코드 꼭 추가하세요!
            }
        }

        // Entity 변환 (Mapper에서 profileImg를 꺼내 쓰도록 되어 있어야 함)
        User user = UserMapper.mapToUser(requestDto);

        // 닉네임 중복 체크
        isNicknameAvailable(user.getNickname(), null);

        User savedUser = userRepository.save(user);
        return UserMapper.mapToUserResponse(savedUser);
    }

    /**
     * 2. 로그인 및 토큰 발급 (Security 버전 유지)
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponseDto login(String email, String password) {
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, password);
            Authentication authentication = authenticationManager.authenticate(authenticationToken);

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
            throw new BusinessException(ErrorCode.AUTH_INVALID_CREDENTIALS);
        }
    }

    /**
     * 3. 이메일 유효성 및 중복 확인 (Controller 버전 유지)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean isEmailAvailable(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (email == null || !email.matches(regex)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        return true;
    }

    /**
     * 4. 전화번호 가용성 체크 (Controller 버전 유지)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean isPhoneAvailable(String phoneNumber, Long userId) {
        if (phoneNumber == null) throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);
        String targetPhone = phoneNumber.trim();
        if (!targetPhone.matches("^\\d{11}$")) throw new BusinessException(ErrorCode.INVALID_PHONE_FORMAT);

        boolean isDuplicate = (userId == null) ?
                userRepository.existsByPhoneNumber(targetPhone) :
                userRepository.existsByPhoneNumberAndIdNot(targetPhone, userId);

        if (isDuplicate) throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        return true;
    }

    /**
     * 5. 닉네임 가용성 체크 (Controller 버전 유지)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean isNicknameAvailable(String nickname, Long currentUserId) {
        if (nickname == null) throw new BusinessException(ErrorCode.USER_NICKNAME_FORMAT_INVALID);
        String targetNickname = nickname.trim();
        if (!targetNickname.matches("^[a-zA-Z0-9가-힣]{2,10}$")) throw new BusinessException(ErrorCode.USER_NICKNAME_FORMAT_INVALID);

        boolean isDuplicate = (currentUserId == null) ?
                userRepository.existsByNicknameIgnoreCase(targetNickname) :
                userRepository.existsByNicknameIgnoreCaseAndIdNot(targetNickname, currentUserId);

        if (isDuplicate) throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        return true;
    }

    /**
     * 6. 아이디(이메일) 찾기 (공통)
     */
    @Transactional(readOnly = true)
    @Override
    public String findEmailByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, phoneNumber));
        return user.getEmail();
    }

    /**
     * 7. 비밀번호 찾기 (Controller의 반환값 + Security의 암호화 저장 통합)
     */
    @Override
    public String resetPassword(String email, String phoneNumber) {
        User user = userRepository.findByEmailAndPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_MATCHED));

        String tempPassword = generateTempPassword();

        // 💡 Security 버전의 필수 로직: DB에 저장할 때는 반드시 암호화!
        user.updatePassword(passwordEncoder.encode(tempPassword));

        // Controller 버전의 필수 로직: 생성된 임시 비밀번호를 화면에 보여주기 위해 평문 반환
        return tempPassword;
    }

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