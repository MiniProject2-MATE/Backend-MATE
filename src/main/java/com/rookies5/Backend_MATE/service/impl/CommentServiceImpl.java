package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.CommentRequestDto;
import com.rookies5.Backend_MATE.dto.response.CommentResponseDto;
import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Comment;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.mapper.CommentMapper;
import com.rookies5.Backend_MATE.repository.BoardPostRepository;
import com.rookies5.Backend_MATE.repository.CommentRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
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
        // 댓글이 달릴 게시글과 작성자 존재 여부 확인
        BoardPost post = boardPostRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + requestDto.getPostId()));

        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + requestDto.getAuthorId()));

        // DTO -> Entity 변환 (Mapper 활용)
        Comment comment = CommentMapper.mapToEntity(requestDto, post, author);

        // DB 저장
        Comment savedComment = commentRepository.save(comment);

        // Entity -> Response DTO 변환 후 반환
        return CommentMapper.mapToResponse(savedComment);
    }

    /**
     * 2. 특정 게시글의 모든 댓글 조회 (작성일 기준 오름차순)
     */
    @Transactional(readOnly = true)
    @Override
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        return commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. 댓글 수정
     */
    @Override
    public CommentResponseDto updateComment(Long commentId, CommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // Dirty Checking: 댓글 내용 업데이트
        comment.updateComment(requestDto.getContent());

        return CommentMapper.mapToResponse(comment);
    }

    /**
     * 4. 댓글 삭제
     */
    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId));

        commentRepository.delete(comment);
    }
}