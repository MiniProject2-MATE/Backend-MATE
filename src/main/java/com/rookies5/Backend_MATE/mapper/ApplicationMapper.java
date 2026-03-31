package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.dto.request.ApplicationRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.entity.Application;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;

public class ApplicationMapper {

    /**
     * Entity -> Response DTO 변환
     * 프로젝트 방장이 지원 내역을 확인할 때 지원자의 닉네임과 포지션 정보를 함께 제공합니다.
     */
    public static ApplicationResponseDto mapToResponse(Application application) {
        return ApplicationResponseDto.builder()
                .id(application.getId())
                .projectId(application.getProject().getId())
                .applicantId(application.getApplicant().getId())
                .applicantNickname(application.getApplicant().getNickname()) // 지원자 닉네임 추가
                .applicantPosition(application.getApplicant().getPosition().name()) // 지원자 포지션 추가
                .message(application.getMessage())
                .status(application.getStatus())
                .createdAt(application.getAppliedAt())
                .build();
    }

    /**
     * Request DTO -> Entity 변환
     * 새로운 프로젝트 지원서를 생성하며, 초기 상태는 'PENDING(대기)'으로 설정합니다.
     */
    public static Application mapToEntity(ApplicationRequestDto requestDto, Project project, User applicant) {
        return Application.builder()
                .project(project)
                .applicant(applicant)
                .message(requestDto.getMessage())
                .status(ApplicationStatus.PENDING) // 지원 시 초기 상태는 대기
                .build();
    }
}