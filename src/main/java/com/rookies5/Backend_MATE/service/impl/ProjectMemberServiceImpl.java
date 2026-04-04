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
     * 팀원 강제 퇴출 (OWNER 전용)
     */
    @Override
    @Transactional
    public void removeMember(Long memberId, Long currentUserId) {
        // 1. 대상 멤버 존재 확인
        ProjectMember targetMember = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND, memberId));

        Project project = targetMember.getProject();
        Long ownerId = project.getOwner().getId();

        // 2. 권한 체크: 요청자가 해당 프로젝트의 방장(OWNER)인지 확인
        if (!ownerId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "팀원 퇴출은 방장만 가능합니다.");
        }

        // 3. 방장 본인은 퇴출 대상이 될 수 없음 (방어 로직)
        if (targetMember.getUser().getId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "방장 본인을 퇴출할 수 없습니다. 프로젝트 삭제를 이용하세요.");
        }

        // 4. 인원수 감소 및 데이터 삭제
        project.decreaseCurrentCount();
        projectMemberRepository.delete(targetMember);
    }
}