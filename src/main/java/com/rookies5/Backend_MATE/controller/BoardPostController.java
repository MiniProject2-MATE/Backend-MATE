package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.BoardPostRequestDto;
import com.rookies5.Backend_MATE.dto.response.BoardPostResponseDto;
import com.rookies5.Backend_MATE.security.CustomUserDetails;
import com.rookies5.Backend_MATE.service.BoardPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BoardPostController {

    private final BoardPostService boardPostService;

    /**
     * 새로운 게시글 작성
     */
    @PostMapping("/{projectId}/board")
    public SuccessResponse<BoardPostResponseDto> createPost(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BoardPostRequestDto requestDto) {

        // DTO에 URL의 projectId를 설정하여 일관성 유지
        requestDto.setProjectId(projectId);

        log.info("게시글 작성 요청 - projectId: {}, userId: {}", projectId, userDetails.getId());

        // Service의 createPost(userId, requestDto) 형식에 맞춤
        BoardPostResponseDto responseDto = boardPostService.createPost(userDetails.getId(), requestDto);

        return new SuccessResponse<>("게시글이 성공적으로 작성되었습니다.", responseDto);
    }

    /**
     * 특정 프로젝트의 모든 게시글 조회
     */
    @GetMapping("/{projectId}/board")
    public SuccessResponse<List<BoardPostResponseDto>> getPostsByProjectId(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("프로젝트 게시글 목록 조회 요청 - projectId: {}, userId: {}", projectId, userDetails.getId());

        // Service의 getPostsByProjectId(projectId, userId) 형식에 맞춤
        List<BoardPostResponseDto> responseDtoList = boardPostService.getPostsByProjectId(projectId, userDetails.getId());

        return new SuccessResponse<>("게시글 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 특정 프로젝트의 게시글 상세 조회 (조회수 증가 및 권한 검증 포함)
     */
    @GetMapping("/{projectId}/board/{postId}")
    public SuccessResponse<BoardPostResponseDto> getPostDetail(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("게시글 상세 조회 요청 - projectId: {}, postId: {}, userId: {}", projectId, postId, userDetails.getId());

        // Service의 getPostDetail(projectId, postId, userId) 형식에 맞춤
        BoardPostResponseDto responseDto = boardPostService.getPostDetail(projectId, postId, userDetails.getId());

        return new SuccessResponse<>("게시글 조회가 완료되었습니다.", responseDto);
    }

    /**
     * 게시글 부분 수정 (PATCH)
     */
    @PatchMapping("/{projectId}/board/{postId}")
    public SuccessResponse<BoardPostResponseDto> patchPost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody BoardPostRequestDto requestDto) {

        log.info("게시글 수정 요청 - postId: {}, userId: {}", postId, userDetails.getId());

        // Service의 patchPost(postId, userId, requestDto) 형식에 맞춤
        BoardPostResponseDto responseDto = boardPostService.patchPost(postId, userDetails.getId(), requestDto);

        return new SuccessResponse<>("게시글이 수정되었습니다.", responseDto);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{projectId}/board/{postId}")
    public SuccessResponse<Void> deletePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("게시글 삭제 요청 - postId: {}, userId: {}", postId, userDetails.getId());

        // Service의 deletePost(postId, userId) 형식에 맞춤
        boardPostService.deletePost(postId, userDetails.getId());

        return new SuccessResponse<>("게시글이 성공적으로 삭제되었습니다.");
    }
}