package com.rookies5.Backend_MATE.service;

import java.util.List;

public interface CommentService {
    CommentDto createComment(CommentDto commentDto);
    List<CommentDto> getCommentsByPostId(Long postId);
    CommentDto updateComment(Long commentId, CommentDto updatedComment);
    void deleteComment(Long commentId);
}