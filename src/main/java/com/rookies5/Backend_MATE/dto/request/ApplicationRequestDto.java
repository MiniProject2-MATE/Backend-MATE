package com.rookies5.Backend_MATE.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequestDto {
    private Long projectId;

    private Long applicantId;
    
    @NotBlank(message = "지원 동기는 필수 입력 항목입니다.")
    private String message;
}