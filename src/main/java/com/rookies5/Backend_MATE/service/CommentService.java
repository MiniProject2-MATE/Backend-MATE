package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.CommentDto;
import java.util.List;

public interface CommentService {
    CommentDto createComment(CommentDto commentDto);
    List<CommentDto> getCommentsByPostId(Long postId);
    CommentDto updateComment(Long commentId, CommentDto updatedComment);
    void deleteComment(Long commentId);
}