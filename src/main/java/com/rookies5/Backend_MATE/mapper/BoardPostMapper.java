package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;

public class BoardPostMapper {
    public static BoardPostDto mapToBoardPostDto(BoardPost post) {
        return BoardPostDto.builder()
                .id(post.getId())
                .projectId(post.getProject().getId())
                .authorId(post.getAuthor().getId())
                .title(post.getTitle())
                .content(post.getContent())
                .viewCount(post.getViewCount())
                .build();
    }

    public static BoardPost mapToBoardPost(BoardPostDto dto, Project project, User author) {
        return BoardPost.builder()
                .project(project)
                .author(author)
                .title(dto.getTitle())
                .content(dto.getContent())
                .viewCount(0) // 초기 조회수 0
                .build();
    }
}