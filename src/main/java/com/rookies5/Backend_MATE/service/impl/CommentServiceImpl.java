package com.rookies5.Backend_MATE.service.impl;

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

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final BoardPostRepository boardPostRepository;
    private final UserRepository userRepository;

    // 1. 댓글 작성
    @Override
    public CommentDto createComment(CommentDto commentDto) {
        // 1. 댓글이 달릴 게시글과 작성자 찾기
        BoardPost post = boardPostRepository.findById(commentDto.getPostId())
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + commentDto.getPostId()));

        User author = userRepository.findById(commentDto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + commentDto.getAuthorId()));

        // 2. DTO -> Entity 변환
        Comment comment = CommentMapper.mapToComment(commentDto, post, author);
        
        // 3. DB 저장
        Comment savedComment = commentRepository.save(comment);

        // 4. 저장된 Entity -> DTO 변환 후 리턴
        return CommentMapper.mapToCommentDto(savedComment);
    }

    // 2. 특정 게시글의 모든 댓글 조회 (오래된 순으로 정렬)
    @Transactional(readOnly = true)
    @Override
    public List<CommentDto> getCommentsByPostId(Long postId) {
        // 레포지토리에 findAllByPostIdOrderByCreatedAtAsc(postId) 메서드가 있다고 가정
        List<Comment> comments = commentRepository.findAllByPostIdOrderByCreatedAtAsc(postId);

        return comments.stream()
                .map(CommentMapper::mapToCommentDto)
                .toList();
    }

    // 3. 댓글 수정
    @Override
    public CommentDto updateComment(Long commentId, CommentDto updatedComment) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // Dirty Checking: 댓글은 '내용(content)'만 수정 가능합니다.
        comment.updateComment(updatedComment.getContent());

        return CommentMapper.mapToCommentDto(comment);
    }

    // 4. 댓글 삭제 (Soft Delete는 엔티티의 @Where 설정에 의해 자동 적용됨)
    @Override
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다. ID: " + commentId));

        commentRepository.delete(comment);
    }
}