package com.rookies5.Backend_MATE.dto.response;

import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ApplicationResponseDto {
    private Long id;
    private Long projectId;
    private Long applicantId;
    private String message;

    private String applicantNickname;
    private String applicantPosition;

    private ApplicationStatus status; // 여기서 상태값을 보여줍니다.

    private LocalDateTime createdAt;
}