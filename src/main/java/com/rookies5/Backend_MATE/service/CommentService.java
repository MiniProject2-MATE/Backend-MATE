package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.CommentRequestDto;
import com.rookies5.Backend_MATE.dto.response.CommentResponseDto;

import java.util.List;

public interface CommentService {

    /**
     * 댓글 작성
     * @param requestDto 댓글 내용 등 입력 정보
     * @return 생성된 댓글 정보 (작성자 닉네임, 생성 시간 포함)
     */
    CommentResponseDto createComment(CommentRequestDto requestDto);

    /**
     * 특정 게시글의 모든 댓글 조회
     * @param postId 게시글 ID
     * @return 해당 게시글에 달린 댓글 리스트
     */
    List<CommentResponseDto> getCommentsByPostId(Long postId);

    /**
     * 댓글 수정
     * @param commentId 수정할 댓글 ID
     * @param requestDto 수정할 내용
     * @return 수정된 댓글 정보
     */
    CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto);

    /**
     * 댓글 삭제
     * @param commentId 삭제할 댓글 ID
     */
    void deleteComment(Long commentId);
}