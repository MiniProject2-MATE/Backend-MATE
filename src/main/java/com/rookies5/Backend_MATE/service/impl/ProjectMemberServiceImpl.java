package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.response.ProjectMemberResponseDto;
import com.rookies5.Backend_MATE.entity.ProjectMember;
import com.rookies5.Backend_MATE.entity.enums.MemberRole;
import com.rookies5.Backend_MATE.mapper.ProjectMemberMapper;
import com.rookies5.Backend_MATE.repository.ProjectMemberRepository;
import com.rookies5.Backend_MATE.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;

    /**
     * 1. 특정 프로젝트의 참여 멤버 목록 조회
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponseDto> getMembersByProjectId(Long projectId) {
        return projectMemberRepository.findAllByProjectId(projectId).stream()
                // 💡 mapToProjectMemberDto -> mapToResponse로 변경
                .map(ProjectMemberMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 2. 팀원 강제 퇴출 또는 자진 탈퇴
     */
    @Override
    public void removeMember(Long memberId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 팀원을 찾을 수 없습니다. ID: " + memberId));

        // ⚠️ 비즈니스 규칙: 방장은 프로젝트를 탈퇴하거나 강퇴당할 수 없음
        if (member.getRole() == MemberRole.OWNER) {
            throw new RuntimeException("방장은 프로젝트를 탈퇴할 수 없습니다. 방장 권한을 위임하거나 프로젝트를 삭제해야 합니다.");
        }

        projectMemberRepository.delete(member);

        // TODO: 실제 로직 구현 시 Project 엔티티의 currentCount를 1 감소시키는 로직 추가 권장
    }
}