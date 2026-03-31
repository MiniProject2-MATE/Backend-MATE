package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.response.ProjectMemberResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.ProjectMember;
import com.rookies5.Backend_MATE.entity.enums.MemberRole;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.ProjectMemberMapper;
import com.rookies5.Backend_MATE.repository.ProjectMemberRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
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
    // 멤버 목록 조회 시 프로젝트 존재 여부를 검증하기 위해 추가됨
    private final ProjectRepository projectRepository;

    /**
     * 1. 특정 프로젝트의 참여 멤버 목록 조회
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectMemberResponseDto> getMembersByProjectId(Long projectId) {
        // 프로젝트 존재 여부 사전 검증
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId);
        }

        return projectMemberRepository.findAllByProjectId(projectId).stream()
                .map(ProjectMemberMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 2. 팀원 강제 퇴출 또는 자진 탈퇴
     */
    @Override
    public void removeMember(Long memberId) {
        // 멤버 존재 여부 확인 예외처리
        ProjectMember member = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND, memberId));

        // 권한 체크: 방장은 탈퇴 불가
        if (member.getRole() == MemberRole.OWNER) {
            // detail 필드를 활용하여 구체적인 이유 명시
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "방장은 프로젝트를 탈퇴할 수 없습니다.");
        }

        // 인원수 감소 로직
        Project project = member.getProject();
        project.decreaseCurrentCount();

        // 멤버 테이블에서 해당 데이터 삭제
        projectMemberRepository.delete(member);
    }
}