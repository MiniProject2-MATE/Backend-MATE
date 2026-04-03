package com.rookies5.Backend_MATE.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BoardPostResponseDto {
    private Long id;
    private Long projectId;
    private Long authorId;
    private String authorNickname; // 작성자 이름
    private String title;
    private String content;
    private Integer viewCount;
    private LocalDateTime createdAt; // BaseEntity의 필드
    private boolean isAuthor;
}