package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.CommentRequestDto;
import com.rookies5.Backend_MATE.dto.response.CommentResponseDto;
import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Comment;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.CommentMapper;
import com.rookies5.Backend_MATE.repository.BoardPostRepository;
import com.rookies5.Backend_MATE.repository.CommentRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.security.SecurityUtils;
import com.rookies5.Backend_MATE.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;

    /**
     * 1. 댓글 작성
     */
    @Override
    public CommentResponseDto createComment(CommentRequestDto requestDto) {
        // 댓글이 달릴 게시글 존재 여부 확인 예외처리
        BoardPost post = boardPostRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, requestDto.getPostId()));

        // 작성자 존재 여부 확인 예외처리
        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, requestDto.getAuthorId()));

        // DTO -> Entity 변환
        Comment comment = CommentMapper.mapToEntity(requestDto, post, author);

        // DB 저장
        Comment savedComment = commentRepository.save(comment);

        // Entity -> Response DTO 반환
        return CommentMapper.mapToResponse(savedComment);
    }

    /**
     * 2. 특정 게시글의 모든 댓글 조회 (작성일 기준 오름차순)
     */
    @Transactional(readOnly = true)
    @Override
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        // (선택사항) 게시글이 실제로 존재하는지 먼저 검증
        if (!boardPostRepository.existsById(postId)) {
            throw new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, postId);
        }

        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. 댓글 수정
     */
    @Override
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto) {
        // 댓글 존재 여부 확인 예외처리
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COMMENT_NOT_FOUND, commentId));

        // 권한 검증: 요청한 사용자가 실제 댓글 작성자인지 확인
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!comment.getAuthor().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "댓글 수정 권한이 없습니다.");
        }

        // Dirty Checking: 댓글 내용 업데이트
        comment.updateComment(requestDto.getContent());

        return CommentMapper.mapToResponse(comment);
    }

    /**
     * 4. 댓글 삭제
     */
    @Override
    public void deleteComment(Long commentId) {
        // 댓글 존재 여부 확인 예외처리
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COMMENT_NOT_FOUND, commentId));

        // 권한 검증: 댓글 작성자 본인이거나 해당 프로젝트의 방장인 경우에만 삭제 가능
        Long currentUserId = SecurityUtils.getCurrentUserId();
        boolean isAuthor = comment.getAuthor().getId().equals(currentUserId);
        boolean isProjectOwner = comment.getPost().getProject().getOwner().getId().equals(currentUserId);

        if (!isAuthor && !isProjectOwner) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}