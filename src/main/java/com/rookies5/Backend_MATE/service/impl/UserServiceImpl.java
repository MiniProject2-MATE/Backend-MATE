package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
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
            throw new RuntimeException("USER_001: 이미 가입된 이메일입니다.");
        }
        if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
            throw new RuntimeException("USER_002: 이미 사용 중인 전화번호입니다.");
        }
        if (requestDto.getNickname() != null && checkNicknameDuplicate(requestDto.getNickname())) {
            throw new RuntimeException("USER_003: 이미 사용 중인 닉네임입니다.");
        }

        // 💡 UserMapper.mapToUser로 수정 (매퍼의 메서드명과 일치)
        User user = UserMapper.mapToUser(requestDto);
        User savedUser = userRepository.save(user);

        // 💡 UserMapper.mapToUserResponse로 수정 (매퍼의 메서드명과 일치)
        return UserMapper.mapToUserResponse(savedUser);
    }

    /**
     * 2. 사용자 단건 조회
     */
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(Long userId) {
        return userRepository.findById(userId)
                // 💡 UserMapper.mapToUserResponse로 수정
                .map(UserMapper::mapToUserResponse)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    /**
     * 3. 전체 회원 조회
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                // 💡 UserMapper.mapToUserResponse로 수정
                .map(UserMapper::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * 4. 회원 정보 수정
     */
    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (requestDto.getNickname() != null &&
                !user.getNickname().equals(requestDto.getNickname()) &&
                checkNicknameDuplicate(requestDto.getNickname())) {
            throw new RuntimeException("USER_003: 이미 사용 중인 닉네임입니다.");
        }

        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getPosition(),
                requestDto.getTechStacks(),
                requestDto.getProfileImg(),
                requestDto.getPhoneNumber()
        );

        // 💡 UserMapper.mapToUserResponse로 수정
        return UserMapper.mapToUserResponse(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));
        userRepository.delete(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}