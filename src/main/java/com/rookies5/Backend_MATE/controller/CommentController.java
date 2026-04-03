package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.security.CustomUserDetails;
import com.rookies5.Backend_MATE.dto.request.CommentRequestDto;
import com.rookies5.Backend_MATE.dto.response.CommentResponseDto;
import com.rookies5.Backend_MATE.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts") // 게시글 하위 리소스로 변경
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 1. 댓글 작성
     * POST /api/posts/{postId}/comments
     */
    @PostMapping("/{postId}/comments")
    public SuccessResponse<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentRequestDto requestDto) {

        log.info("댓글 작성 요청 - postId: {}, userId: {}", postId, userDetails.getId());

        // 서비스에 postId, userId, DTO 전달
        CommentResponseDto responseDto = commentService.createComment(postId, userDetails.getId(), requestDto);

        return new SuccessResponse<>("댓글이 성공적으로 작성되었습니다.", responseDto);
    }

    /**
     * 2. 특정 게시글의 모든 댓글 조회
     * GET /api/posts/{postId}/comments
     */
    @GetMapping("/{postId}/comments")
    public SuccessResponse<List<CommentResponseDto>> getCommentsByPostId(
            @PathVariable Long postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("게시글 댓글 목록 조회 요청 - postId: {}, userId: {}", postId, userDetails.getId());

        // 멤버 검증을 위해 userId도 함께 전달
        List<CommentResponseDto> responseDtoList = commentService.getCommentsByPostId(postId, userDetails.getId());

        return new SuccessResponse<>("댓글 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 3. 댓글 수정
     * PUT /api/posts/comments/{commentId}
     */
    @PutMapping("/comments/{commentId}")
    public SuccessResponse<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CommentRequestDto requestDto) {

        log.info("댓글 수정 요청 - commentId: {}, userId: {}", commentId, userDetails.getId());

        CommentResponseDto responseDto = commentService.updateComment(commentId, userDetails.getId(), requestDto);

        return new SuccessResponse<>("댓글이 성공적으로 수정되었습니다.", responseDto);
    }

    /**
     * 4. 댓글 삭제
     * DELETE /api/posts/comments/{commentId}
     */
    @DeleteMapping("/comments/{commentId}")
    public SuccessResponse<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("댓글 삭제 요청 - commentId: {}, userId: {}", commentId, userDetails.getId());

        commentService.deleteComment(commentId, userDetails.getId());

        return new SuccessResponse<>("댓글이 성공적으로 삭제되었습니다.");
    }
}