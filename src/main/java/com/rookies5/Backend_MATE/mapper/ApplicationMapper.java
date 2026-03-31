package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.entity.Application;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;

public class ApplicationMapper {
    public static ApplicationDto mapToApplicationDto(Application application) {
        return ApplicationDto.builder()
                .id(application.getId())
                .projectId(application.getProject().getId())
                .applicantId(application.getApplicant().getId())
                .message(application.getMessage())
                .status(application.getStatus())
                .build();
    }

    public static Application mapToApplication(ApplicationDto dto, Project project, User applicant) {
        return Application.builder()
                .project(project)
                .applicant(applicant)
                .message(dto.getMessage())
                .status(ApplicationStatus.PENDING) // 지원 시 무조건 대기 상태
                .build();
    }
}