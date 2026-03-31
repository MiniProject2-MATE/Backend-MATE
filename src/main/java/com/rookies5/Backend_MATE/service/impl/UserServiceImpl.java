package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    /**
     * 1. 회원가입
     */
    @Override
    public UserResponseDto createUser(UserRequestDto requestDto) {
        if (userRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            throw new BusinessException(ErrorCode.USER_EMAIL_DUPLICATE);
        }
        if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
        }
        if (requestDto.getNickname() != null && checkNicknameDuplicate(requestDto.getNickname())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        User user = UserMapper.mapToUser(requestDto);
        User savedUser = userRepository.save(user);

        return UserMapper.mapToUserResponse(savedUser);
    }

    /**
     * 2. 사용자 단건 조회
     */
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::mapToUserResponse)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    /**
     * 3. 전체 회원 조회
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                // UserMapper.mapToUserResponse로 수정
                .map(UserMapper::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * 4. 회원 정보 수정
     */
    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        if (requestDto.getNickname() != null &&
                !user.getNickname().equals(requestDto.getNickname()) &&
                checkNicknameDuplicate(requestDto.getNickname())) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getPosition(),
                requestDto.getTechStacks(),
                requestDto.getProfileImg(),
                requestDto.getPhoneNumber()
        );

        // UserMapper.mapToUserResponse로 수정
        return UserMapper.mapToUserResponse(user);
    }

    /**
     * 5. 회원 탈퇴
     */
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 추후 추가할 비즈니스 로직: 진행 중인 프로젝트의 방장이거나 팀원인지 확인 (선택사항)
        // if (user.hasActiveProjects()) {
        //     throw new BusinessException(ErrorCode.USER_ACTIVE_PROJECT_EXISTS);
        // }

        userRepository.delete(user);
    }

    /**
     * 6. 닉네임 중복 확인 공통 메서드
     */
    @Override
    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 7. 아이디(이메일) 찾기
     */
    @Override
    @Transactional(readOnly = true)
    public String findEmailByPhoneNumber(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                // 전화번호로 가입된 유저가 없으면 에러 발생
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, phoneNumber));

        // (선택사항) 보안을 위해 이메일 일부를 마스킹 처리할 수 있습니다. (예: abc***@gmail.com)
        // 지금은 그대로 반환합니다.
        return user.getEmail();
    }

    /**
     * 8. 비밀번호 재설정 (임시 비밀번호 발급 및 반환)
     */
    @Override
    public String resetPassword(String email, String phoneNumber) {
        // 1. 입력받은 이메일과 전화번호가 일치하는 회원이 있는지 검증
        User user = userRepository.findByEmailAndPhoneNumber(email, phoneNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_MATCHED));

        // 2. 임시 비밀번호 8자리 생성
        String tempPassword = generateTempPassword();

        // 3. 엔티티 비밀번호 업데이트 (더티 체킹)
        // 주의: 나중에 Spring Security를 달면 반드시 passwordEncoder.encode(tempPassword) 로 암호화해서 넣어야 합니다!
        user.updatePassword(tempPassword);

        // 4. 콘솔 출력이 아닌, 생성된 임시 비밀번호를 직접 반환합니다.
        return tempPassword;
    }

    /**
     * 내부 유틸 메서드: 8자리 영문+숫자 임시 비밀번호 생성기
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