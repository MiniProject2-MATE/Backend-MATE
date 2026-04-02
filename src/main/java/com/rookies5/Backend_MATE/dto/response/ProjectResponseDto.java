package com.rookies5.Backend_MATE.dto.response;

import com.rookies5.Backend_MATE.entity.enums.Category;
import com.rookies5.Backend_MATE.entity.enums.OnOffline;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ProjectResponseDto {
    private Long id;
    private Long ownerId;
    private String ownerNickname; // 방장 이름
    
    private Category category;
    private String title;
    private String content;
    
    private Integer recruitCount;
    private Integer currentCount;
    private ProjectStatus status;
    private OnOffline onOffline;
    
    private LocalDate endDate;
    private Long remainingDays; // 마감까지 남은 일수 (D-Day 계산용)
    private LocalDateTime createdAt;
}