package com.rookies5.Backend_MATE.dto;

import com.rookies5.Backend_MATE.entity.enums.Category;
import com.rookies5.Backend_MATE.entity.enums.OnOffline;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDto {
    private Long id;
    
    private Long ownerId; // 연관관계: 방장(User)의 ID

    @NotNull(message = "카테고리는 필수입니다.")
    private Category category;

    @NotBlank(message = "제목은 필수 입력 항목입니다.")
    private String title;

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    private String content;

    @NotNull(message = "모집 인원은 필수입니다.")
    private Integer recruitCount;

    private Integer currentCount; // 매퍼에서 기본값 0 처리
    
    private ProjectStatus status; // 매퍼에서 기본값 RECRUITING 처리

    @NotNull(message = "진행 방식은 필수입니다.")
    private OnOffline onOffline;

    @NotNull(message = "마감일은 필수입니다.")
    private LocalDate endDate;
}