package com.rookies5.Backend_MATE.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardPostResponseDto {
    private Long id;
    private Long projectId;
    private String projectTitle; // 어떤 프로젝트의 게시판인지 제목 추가
    private Long authorId;
    private String authorNickname; // 작성자 이름
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt; // BaseEntity의 필드
}