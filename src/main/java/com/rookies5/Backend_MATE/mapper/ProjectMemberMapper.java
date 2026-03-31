package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.ProjectMember;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.MemberRole;

public class ProjectMemberMapper {

    // Entity -> DTO 변환 (화면에 멤버 목록 뿌려줄 때)
    public static ProjectMemberDto mapToProjectMemberDto(ProjectMember member) {
        return ProjectMemberDto.builder()
                .id(member.getId())
                .projectId(member.getProject().getId())
                .userId(member.getUser().getId())
                .role(member.getRole())
                // User 엔티티에 접근해서 프론트엔드용 정보 추출!
                .nickname(member.getUser().getNickname())
                .profileImg(member.getUser().getProfileImg())
                .position(member.getUser().getPosition())
                .build();
    }

    // 객체 조립용 (방장이 지원서를 승인해서 새로운 팀원을 DB에 저장할 때 사용)
    public static ProjectMember mapToProjectMember(Project project, User user, MemberRole role) {
        return ProjectMember.builder()
                .project(project)
                .user(user)
                .role(role)
                .build();
    }
}