package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.UserDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    // private final PasswordEncoder passwordEncoder; // 실제 로그인 구현 시 주입 필요

    // 1. 회원가입 (createUser)
    @Override
    public UserDto createUser(UserDto userDto) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("USER_001: 이미 가입된 이메일입니다.");
        }

        // 전화번호 중복 체크
        if (userRepository.existsByPhoneNumber(userDto.getPhoneNumber())) {
            throw new RuntimeException("USER_002: 이미 사용 중인 전화번호입니다.");
        }

        // 닉네임 중복 확인 (추가!)
        if (userDto.getNickname() != null && checkNicknameDuplicate(userDto.getNickname())) {
            throw new RuntimeException("USER_003: 이미 사용 중인 닉네임입니다.");
        }

        // 닉네임 자동 할당 (없을 경우에만)
        if (userDto.getNickname() == null || userDto.getNickname().isBlank()) {
            String autoNickname = userDto.getEmail().split("@")[0];
            userDto.setNickname(autoNickname);
        }

        User user = UserMapper.mapToUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.mapToUserDto(savedUser);
    }

    // 4.1.4 아이디(이메일) 찾기
    @Transactional(readOnly = true)
    public String findEmailByPhone(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("AUTH_003: 해당 번호로 가입된 정보를 찾을 수 없습니다."));

        // 설계서 3.10.2: 이메일 마스킹 처리 (예: jiho****@gmail.com)
        String email = user.getEmail();
        int atIndex = email.indexOf("@");
        return email.substring(0, 4) + "****" + email.substring(atIndex);
    }

    // 단건 조회 (마이페이지 등)
    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    // 전체 회원 조회 (관리자용)
    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .toList();
    }

    // 4. 회원 정보 수정 (updateUser)
    @Override
    public UserDto updateUser(Long userId, UserDto updatedUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 닉네임을 변경하려고 할 때만 중복 확인
        // (현재 내 닉네임과 다른데, 이미 DB에 그 이름이 있다면 에러!)
        if (!user.getNickname().equals(updatedUser.getNickname()) &&
                checkNicknameDuplicate(updatedUser.getNickname())) {
            throw new RuntimeException("USER_003: 이미 사용 중인 닉네임입니다.");
        }

        user.updateProfile(
                updatedUser.getNickname(),
                updatedUser.getPosition(),
                updatedUser.getTechStacks(),
                updatedUser.getProfileImg(),
                updatedUser.getPhoneNumber()
        );

        return UserMapper.mapToUserDto(user);
    }

    // 회원 탈퇴 (Delete - 설계서의 Soft Delete 로직 적용)
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 💡 Repository에 @SQLDelete 설정이 되어있다면 자동으로 Soft Delete 처리됩니다.
        userRepository.delete(user);
    }

    // 4.1.7 닉네임 중복 확인 공통 메서드
    @Override
    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}