package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;

public class ProjectMapper {

    // Entity -> DTO 변환
    public static ProjectDto mapToProjectDto(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .category(project.getCategory())
                .title(project.getTitle())
                .content(project.getContent())
                .recruitCount(project.getRecruitCount())
                .currentCount(project.getCurrentCount())
                .status(project.getStatus())
                .onOffline(project.getOnOffline())
                .endDate(project.getEndDate())
                .build();
    }

    // DTO -> Entity 변환 (저장할 때 쓰임, 연관된 User 객체를 파라미터로 같이 받음)
    public static Project mapToProject(ProjectDto projectDto, User owner) {
        return Project.builder()
                // id는 auto_increment이므로 보통 새로 생성할 땐 뺍니다.
                .owner(owner) // DTO에 있는 ownerId 대신 실제 User 엔티티를 세팅
                .category(projectDto.getCategory())
                .title(projectDto.getTitle())
                .content(projectDto.getContent())
                .recruitCount(projectDto.getRecruitCount())
                .currentCount(0) // 처음 생성 시 현재 인원은 0명
                .status(com.rookies5.Backend_MATE.entity.enums.ProjectStatus.RECRUITING) // 기본 상태
                .onOffline(projectDto.getOnOffline())
                .endDate(projectDto.getEndDate())
                .build();
    }
}