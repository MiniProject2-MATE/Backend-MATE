package com.rookies5.Backend_MATE.dto;

import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDto {
    private Long id;
    
    @NotNull(message = "프로젝트 ID는 필수입니다.")
    private Long projectId;
    
    @NotNull(message = "지원자 ID는 필수입니다.")
    private Long applicantId;
    
    @NotBlank(message = "지원 동기는 필수 입력 항목입니다.")
    private String message;
    
    private ApplicationStatus status; // 매퍼에서 기본값 PENDING 처리
}