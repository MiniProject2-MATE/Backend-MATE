package com.rookies5.Backend_MATE.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardPostDto {
    private Long id;
    
    @NotNull(message = "프로젝트 ID는 필수입니다.")
    private Long projectId;
    
    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long authorId;
    
    @NotBlank(message = "제목은 필수입니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    private String content;
    
    private Integer viewCount;
}