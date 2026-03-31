package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.dto.CommentDto;
import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Comment;
import com.rookies5.Backend_MATE.entity.User;

public class CommentMapper {

    // Entity -> DTO 변환 (DB에서 꺼내서 프론트로 보낼 때)
    public static CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .postId(comment.getPost().getId())
                .authorId(comment.getAuthor().getId())
                // User 엔티티에서 직접 닉네임과 프로필 이미지를 뽑아와서 DTO에 담아줍니다.
                .authorNickname(comment.getAuthor().getNickname())
                .authorProfileImg(comment.getAuthor().getProfileImg())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // DTO -> Entity 변환 (프론트에서 입력받아 DB에 저장할 때)
    public static Comment mapToComment(CommentDto commentDto, BoardPost post, User author) {
        return Comment.builder()
                .post(post)
                .author(author)
                .content(commentDto.getContent())
                .build();
    }
}