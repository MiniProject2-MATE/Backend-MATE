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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * 추가: 전체 회원 조회 (관리자용 등)
     * GET /api/users
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    /**
     * 1. 내 정보 상세 조회
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDto> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    /**
     * 2. 내 정보 수정
     */
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long userId, @RequestBody UserRequestDto requestDto) {
        return ResponseEntity.ok(userService.updateUser(userId, requestDto));
    }

    /**
     * 3. 프로필 이미지 수정 (지호님 패턴 맞춤 적용)
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
    public SuccessResponse<Void> deleteProfileImage(
            @RequestParam("userId") Long userId) {

        log.info("프로필 이미지 삭제 요청 - 유저 ID: {}", userId);
        userService.deleteProfileImage(userId);

        return new SuccessResponse<>("프로필 이미지가 기본 이미지로 초기화되었습니다.");
    }

    // --- 마이페이지 활동 이력 3종 세트 ---

    /**
     * 4. 내 모집글 조회 (내가 방장인 것)
     * GET /api/users/{userId}/posts/owned
     */
    @GetMapping("/{userId}/posts/owned")
    public ResponseEntity<List<ProjectResponseDto>> getMyOwnedPosts(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getMyOwnedPosts(userId));
    }

    /**
     * 5. 참여 중인 프로젝트/스터디 조회 (승인 완료)
     * GET /api/users/{userId}/posts/joined
     */
    @GetMapping("/{userId}/posts/joined")
    public ResponseEntity<List<ProjectResponseDto>> getMyJoinedProjects(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getMyJoinedProjects(userId));
    }

    /**
     * 6. 내 신청 현황 조회 (대기/거절 상태)
     * GET /api/users/{userId}/applications/pending
     */
    @GetMapping("/{userId}/applications/pending")
    public ResponseEntity<List<ApplicationResponseDto>> getMyPendingApplications(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getMyPendingApplications(userId));
    }
}