package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.entity.ProjectMember;
import com.rookies5.Backend_MATE.entity.enums.MemberRole;
import com.rookies5.Backend_MATE.mapper.ProjectMemberMapper;
import com.rookies5.Backend_MATE.repository.ProjectMemberRepository;
import com.rookies5.Backend_MATE.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;

    // 1. 특정 프로젝트의 참여 멤버 목록 조회 (모집글 상세, 팀 게시판 등에서 사용)
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberDto> getMembersByProjectId(Long projectId) {
        return projectMemberRepository.findAllByProjectId(projectId).stream()
                .map(ProjectMemberMapper::mapToProjectMemberDto)
                .toList();
    }

    // 2. 팀원 강제 퇴출 또는 자진 탈퇴
    @Override
    public void removeMember(Long memberId) {
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("해당 팀원을 찾을 수 없습니다. ID: " + memberId));

        // ⚠️ 비즈니스 규칙 (MMR-02): 방장은 프로젝트를 탈퇴하거나 강퇴당할 수 없다.
        if (member.getRole() == MemberRole.OWNER) {
            throw new RuntimeException("방장은 프로젝트를 탈퇴할 수 없습니다. 방장 권한을 위임하거나 프로젝트를 삭제해야 합니다.");
        }

        projectMemberRepository.delete(member);
        
        // (참고) 실제 로직에서는 여기서 Project 엔티티의 currentCount(현재 인원)를 1 감소시켜야 합니다!
    }
}