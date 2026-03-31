package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.dto.response.ProjectMemberResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.ProjectMember;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.MemberRole;

public class ProjectMemberMapper {

    // 1. Entity -> Response DTO (팀원 목록 출력용)
    public static ProjectMemberResponseDto mapToResponse(ProjectMember member) {
        return ProjectMemberResponseDto.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                // 💡 엔티티 필드명에 맞춰 userId로 수정 (지호 님 엔티티 기준)
                .userId(member.getUser().getId())
                .role(member.getRole())

                // 💡 지호 님이 강조한 '닉네임'과 '포지션'만 쏙쏙! (프사는 삭제)
                .nickname(member.getUser().getNickname())
                .position(member.getUser().getPosition())
                .build();
    }

    // 2. Entity 조립용 (새로운 팀원 합류 시)
    public static ProjectMember mapToEntity(Project project, User user, MemberRole role) {
        return ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .build();
    }
}