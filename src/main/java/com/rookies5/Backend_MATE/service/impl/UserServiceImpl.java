package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.ApplicationMapper;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.ApplicationRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
// import com.rookies5.Backend_MATE.repository.ProjectRepository;     // 나중에 추가 시 주석 해제
// import com.rookies5.Backend_MATE.repository.ApplicationRepository; // 나중에 추가 시 주석 해제
import com.rookies5.Backend_MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;         // 의존성 주입 필요 시 주석 해제
    private final ApplicationRepository applicationRepository; // 의존성 주입 필요 시 주석 해제

    private static final String DEFAULT_PROFILE_IMG = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";

    /**
     * 1. 내 정보 상세 조회
     */
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::mapToUserResponse)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    /**
     * 2. 전체 회원 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. 내 정보 수정 (닉네임 중복 검사 포함)
     */
    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 닉네임 변경 시에만 중복 체크 실행
        if (requestDto.getNickname() != null && !user.getNickname().equals(requestDto.getNickname())) {
            if (checkNicknameDuplicate(requestDto.getNickname())) {
                throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
            }
        }

        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getPosition(),
                requestDto.getTechStacks(),
                requestDto.getProfileImg(),
                requestDto.getPhoneNumber()
        );

        return UserMapper.mapToUserResponse(user);
    }

    /**
     * 4. 회원 탈퇴 (Hard Delete -> Soft Delete 전환 준비)
     */
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 지호 님, 아직 softDelete 구현 전이라 하셨으니 일단 Hard Delete 유지합니다.
        // 나중에 엔티티에 필드 추가 후 user.softDelete()로 바꾸시면 됩니다!
        userRepository.delete(user);
    }

    /**
     * 5. 닉네임 중복 확인
     */
    @Transactional(readOnly = true)
    @Override
    public boolean checkNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    /**
     * 6. 전화번호 중복 확인
     */
    @Transactional(readOnly = true)
    @Override
    public boolean checkPhoneDuplicate(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 7. 프로필 이미지 수정
     */
    @Override
    public String updateProfileImage(Long userId, MultipartFile profileImage) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // TODO: S3 업로드 로직으로 교체 예정
        String newImgUrl = "https://mate-s3.com/new-profile-" + userId + ".png";
        user.updateProfileImg(newImgUrl);
        return newImgUrl;
    }

    /**
     * 8. 프로필 이미지 삭제 (기본 이미지로 복구)
     */
    @Override
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        user.updateProfileImg(DEFAULT_PROFILE_IMG);
    }

    /**
     * 9. 내가 작성한 모집글 목록 조회 (명세서 4.5.3)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponseDto> getMyPosts(Long userId) {
        // TODO: projectRepository 연동 시 아래 로직 사용
        return projectRepository.findAllByOwnerId(userId).stream()
                .map(ProjectMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 10. 나의 프로젝트 지원 내역 조회 (명세서 4.5.3)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ApplicationResponseDto> getMyApplications(Long userId) {
        // TODO: applicationRepository 연동 시 아래 로직 사용
        return applicationRepository.findAllByUserId(userId).stream()
                .map(ApplicationMapper::mapToResponse)
                .collect(Collectors.toList());
    }
}