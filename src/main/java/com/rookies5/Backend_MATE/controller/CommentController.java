package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.CommentRequestDto;
import com.rookies5.Backend_MATE.dto.response.CommentResponseDto;
import com.rookies5.Backend_MATE.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성
     */
    @PostMapping
    public SuccessResponse<CommentResponseDto> createComment(@Valid @RequestBody CommentRequestDto requestDto) {
        log.info("댓글 작성 요청 - postId: {}, authorId: {}", requestDto.getPostId(), requestDto.getAuthorId());
        CommentResponseDto responseDto = commentService.createComment(requestDto);
        return new SuccessResponse<>("댓글이 성공적으로 작성되었습니다.", responseDto);
    }

    /**
     * 특정 게시글의 모든 댓글 조회
     */
    @GetMapping("/post/{postId}")
    public SuccessResponse<List<CommentResponseDto>> getCommentsByPostId(@PathVariable Long postId) {
        log.info("게시글 댓글 목록 조회 요청 - postId: {}", postId);
        List<CommentResponseDto> responseDtoList = commentService.getCommentsByPostId(postId);
        return new SuccessResponse<>("댓글 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    public SuccessResponse<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequestDto requestDto) {
        log.info("댓글 수정 요청 - commentId: {}", commentId);
        CommentResponseDto responseDto = commentService.updateComment(commentId, requestDto);
        return new SuccessResponse<>("댓글이 성공적으로 수정되었습니다.", responseDto);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    public SuccessResponse<Void> deleteComment(@PathVariable Long commentId) {
        log.info("댓글 삭제 요청 - commentId: {}", commentId);
        commentService.deleteComment(commentId);
        return new SuccessResponse<>("댓글이 성공적으로 삭제되었습니다.");
    }
}