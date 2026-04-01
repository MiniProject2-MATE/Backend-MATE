package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    /**
     * 1. 신규 회원을 등록(회원가입)합니다.
     * 명세서 규칙: 닉네임 미입력 시 자동 생성, 이미지 미첨부 시 기본 이미지 할당(Mapper 담당)
     * @param requestDto 회원가입 폼 데이터
     * @param profileImage 업로드된 프로필 이미지 파일 (선택)
     * @return 생성된 사용자 정보
     */
    @Override
    public UserResponseDto register(UserRequestDto requestDto, MultipartFile profileImage) {
        // 이메일 중복 검증
        if (checkEmailDuplicate(requestDto.getEmail())) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        // 전화번호 중복 검증
        if (checkPhoneDuplicate(requestDto.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        }

        // 이미지 파일이 있을 경우 DTO에 임시 URL 세팅 (실제 S3 연동 전)
        if (profileImage != null && !profileImage.isEmpty()) {
            // TODO: S3 업로드 로직으로 교체 예정
            String profileImgUrl = "https://mate-s3.com/uploaded-" + profileImage.getOriginalFilename();
            requestDto.setProfileImg(profileImgUrl);
        }

        // Mapper를 통해 Entity 변환 (여기서 닉네임 자동추출 및 기본 이미지 처리가 일어남)
        User user = UserMapper.mapToUser(requestDto);

        // 변환된 최종 닉네임으로 중복 검증
        if (checkNicknameDuplicate(user.getNickname())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        User savedUser = userRepository.save(user);
        return UserMapper.mapToUserResponse(savedUser);
    }

    /**
     * 2. 이메일 중복 확인
     * @param email 검사할 이메일
     * @return 중복 여부 (true: 중복, false: 사용 가능)
     */
    @Transactional(readOnly = true)
    @Override
    public boolean checkEmailDuplicate(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     * 3. 닉네임 중복 확인
     * @param nickname 검사할 닉네임
     * @return 중복 여부
     */
    @Transactional(readOnly = true)
    @Override
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 4. 전화번호 중복 확인
     * @param phoneNumber 검사할 전화번호
     * @return 중복 여부
     */
    @Transactional(readOnly = true)
    @Override
    public boolean checkPhoneDuplicate(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 5. 아이디(이메일) 찾기
     * @param phoneNumber 등록된 전화번호
     * @return 사용자의 이메일 주소
     */
    @Transactional(readOnly = true)
    @Override
    public String findEmailByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, phoneNumber));
        return user.getEmail();
    }

    /**
     * 6. 비밀번호 찾기 (임시 비밀번호 발급)
     * @param email 가입 이메일
     * @param phoneNumber 등록된 전화번호
     */
    @Override
    public void resetPassword(String email, String phoneNumber) {
        User user = userRepository.findByEmailAndPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_MATCHED));

        String tempPassword = generateTempPassword();

        // TODO: Spring Security 적용 시 PasswordEncoder.encode(tempPassword) 필수
        user.updatePassword(tempPassword);
    }

    /**
     * 내부 유틸 메서드: 8자리 영문+숫자 혼합 임시 비밀번호 생성
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