package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.dto.request.ApplicationRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.entity.Application;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;

public class ApplicationMapper {

    /**
     * Entity -> Response DTO 변환 (조회용)
     * ★ 수정 포인트: 리스트에 표시할 프로젝트 제목(projectTitle)을 추가합니다.
     */
    public static ApplicationResponseDto mapToApplicationResponse(Application application) {
        return ApplicationResponseDto.builder()
                .id(application.getId())
                .projectId(application.getProject().getId())
                // ★ 지원 내역 탭에서 보여줄 프로젝트 제목 추가!
                .projectTitle(application.getProject().getTitle())
                .applicantId(application.getApplicant().getId())
                .applicantNickname(application.getApplicant().getNickname())
                .applicantPosition(application.getApplicant().getPosition() != null ?
                        application.getApplicant().getPosition().name() : null)
                .message(application.getMessage())
                .status(application.getStatus())
                .createdAt(application.getAppliedAt())
                .build();
    }

    /**
     * Request DTO -> Entity 변환 (저장용)
     */
    public static Application mapToEntity(ApplicationRequestDto requestDto, Project project, User applicant) {
        return Application.builder()
                .project(project)
                .applicant(applicant)
                .message(requestDto.getMessage())
                .status(ApplicationStatus.PENDING)
                .build();
    }
}