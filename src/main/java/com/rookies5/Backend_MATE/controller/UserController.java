package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 0. 전체 회원 조회 (관리자용 등)
     */
    @GetMapping
    public SuccessResponse<List<UserResponseDto>> getAllUsers() {
        log.info("전체 회원 조회 요청");
        List<UserResponseDto> users = userService.getAllUsers();
        return new SuccessResponse<>("전체 회원 조회가 완료되었습니다.", users);
    }

    /**
     * 1. 내 정보 상세 조회
     */
    @GetMapping("/{userId}")
    public SuccessResponse<UserResponseDto> getUserProfile(@PathVariable Long userId) {
        log.info("유저 상세 조회 요청 - ID: {}", userId);
        UserResponseDto user = userService.getUserById(userId);
        return new SuccessResponse<>("유저 정보 조회가 완료되었습니다.", user);
    }

    /**
     * 2. 내 정보 수정
     */
    @PutMapping("/{userId}")
    public SuccessResponse<UserResponseDto> updateUser(@PathVariable Long userId, @RequestBody UserRequestDto requestDto) {
        log.info("유저 정보 수정 요청 - ID: {}", userId);
        UserResponseDto updatedUser = userService.updateUser(userId, requestDto);
        return new SuccessResponse<>("유저 정보가 성공적으로 수정되었습니다.", updatedUser);
    }

    /**
     * 3. 프로필 이미지 수정
     */
    @PatchMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SuccessResponse<String> updateProfileImage(
            @RequestParam("userId") Long userId,
            @RequestPart("profileImage") MultipartFile profileImage) {

        log.info("프로필 이미지 수정 요청 - 유저 ID: {}", userId);
        String newImgUrl = userService.updateProfileImage(userId, profileImage);

        return new SuccessResponse<>("프로필 이미지가 성공적으로 수정되었습니다.", newImgUrl);
    }

    /**
     * 4. 프로필 이미지 삭제 (기본 이미지로 복구)
     */
    @DeleteMapping("/profile-image")
    public SuccessResponse<Void> deleteProfileImage(@RequestParam("userId") Long userId) {
        log.info("프로필 이미지 삭제 요청 - 유저 ID: {}", userId);
        userService.deleteProfileImage(userId);
        return new SuccessResponse<>("프로필 이미지가 기본 이미지로 초기화되었습니다.");
    }

    // --- 마이페이지 활동 이력 3종 세트 ---

    /**
     * 5. 내 모집글 조회 (내가 방장인 것)
     */
    @GetMapping("/{userId}/posts/owned")
    public SuccessResponse<List<ProjectResponseDto>> getMyOwnedPosts(@PathVariable Long userId) {
        log.info("내가 작성한 모집글 조회 - 유저 ID: {}", userId);
        List<ProjectResponseDto> projects = userService.getMyOwnedPosts(userId);
        return new SuccessResponse<>("내가 작성한 모집글 조회가 완료되었습니다.", projects);
    }

    /**
     * 6. 참여 중인 프로젝트/스터디 조회 (승인 완료)
     */
    @GetMapping("/{userId}/posts/joined")
    public SuccessResponse<List<ProjectResponseDto>> getMyJoinedProjects(@PathVariable Long userId) {
        log.info("참여 중인 프로젝트 조회 - 유저 ID: {}", userId);
        List<ProjectResponseDto> projects = userService.getMyJoinedProjects(userId);
        return new SuccessResponse<>("참여 중인 프로젝트 조회가 완료되었습니다.", projects);
    }

    /**
     * 7. 내 신청 현황 조회 (대기/거절 상태)
     */
    @GetMapping("/{userId}/applications/pending")
    public SuccessResponse<List<ApplicationResponseDto>> getMyPendingApplications(@PathVariable Long userId) {
        log.info("내 신청 현황 조회 - 유저 ID: {}", userId);
        List<ApplicationResponseDto> applications = userService.getMyPendingApplications(userId);
        return new SuccessResponse<>("내 신청 현황 조회가 완료되었습니다.", applications);
    }
}