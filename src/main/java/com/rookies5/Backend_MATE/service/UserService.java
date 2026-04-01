package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    /**
     * 1. 특정 사용자 상세 정보 조회 (내 정보 조회 /api/users/me)
     */
    UserResponseDto getUserById(Long userId);

    /**
     * 2. 전체 사용자 목록 조회 (관리자용)
     */
    List<UserResponseDto> getAllUsers();

    /**
     * 3. 사용자 프로필 정보 수정 (닉네임, 포지션, 기술 스택 등)
     */
    UserResponseDto updateUser(Long userId, UserRequestDto requestDto);

    /**
     * 4. 회원 탈퇴 (Soft Delete 방식)
     */
    void deleteUser(Long userId);

    /**
     * 5. 닉네임 중복 체크 (수정 시 실시간 검증용)
     */
    boolean checkNicknameDuplicate(String nickname);

    /**
     * 6. 전화번호 중복 체크 (검증 필요 시 사용)
     */
    boolean checkPhoneDuplicate(String phoneNumber);

    /**
     * 7. 프로필 이미지 단독 수정
     */
    String updateProfileImage(Long userId, MultipartFile profileImage);

    /**
     * 8. 프로필 이미지 삭제 (기본 이미지로 초기화)
     */
    void deleteProfileImage(Long userId);

    /**
     * 9. 내가 작성한 모집글 목록 조회 (명세서 4.5.3)
     * @return ProjectResponseDto 리스트
     */
    List<ProjectResponseDto> getMyPosts(Long userId);

    /**
     * 10. 나의 프로젝트 지원 내역 조회 (명세서 4.5.3)
     * @return ApplicationResponseDto 리스트
     */
    List<ApplicationResponseDto> getMyApplications(Long userId);
}