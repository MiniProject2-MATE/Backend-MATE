package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.ProjectRequestDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.ProjectMember;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;
import com.rookies5.Backend_MATE.entity.enums.MemberRole;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
import com.rookies5.Backend_MATE.repository.ApplicationRepository;
import com.rookies5.Backend_MATE.repository.BoardPostRepository;
import com.rookies5.Backend_MATE.repository.CommentRepository;
import com.rookies5.Backend_MATE.repository.ProjectMemberRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.security.SecurityUtils; // [추가]
import com.rookies5.Backend_MATE.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final BoardPostRepository boardPostRepository;
    private final CommentRepository commentRepository;

    /**
     * 1. 프로젝트 생성 (로그인 유저 기반 자동 설정)
     */
    @Override
    @Transactional
    public ProjectResponseDto createProject(Long userId, ProjectRequestDto requestDto) {
        // 1. 방장(User) 조회
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. Project 엔티티 생성 및 저장
        Project project = ProjectMapper.mapToEntity(requestDto, owner);
        Project savedProject = projectRepository.save(project);

        // 3. 방장을 ProjectMember 테이블에도 저장
        ProjectMember leader = ProjectMember.builder()
                .project(savedProject)
                .user(owner)
                .role(MemberRole.OWNER)
                .build();

        projectMemberRepository.save(leader);

        // 4. 결과 반환
        return ProjectMapper.mapToResponse(savedProject, userId);
    }

    /**
     * 2. 단건 조회 (상세 보기)
     */
    @Transactional(readOnly = true)
    @Override
    public ProjectResponseDto getProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .map(ProjectMapper::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId));
    }

    /**
     * 3. 전체 목록 조회
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponseDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(ProjectMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 4. 프로젝트 부분 수정 (PATCH)
     */
    @Override
    @Transactional
    public ProjectResponseDto patchProject(Long projectId, Long userId, ProjectRequestDto requestDto) {
        // 1. 수정할 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

        // 2. 방장 권한 검증 (컨트롤러에서 넘겨받은 userId 사용)
        if (!project.getOwner().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        // 3. 엔티티 내부의 '부분 수정' 로직 호출
        project.updateProject(requestDto);

        // 4. 변경된 엔티티를 다시 DTO로 변환 (방장 여부 계산을 위해 userId 전달)
        return ProjectMapper.mapToResponse(project, userId);
    }

    /**
     * 5. 삭제 로직
     * 삭제 순서: comments → board_posts → applications → project_members → projects
     * (FK 제약 조건을 위반하지 않도록 자식 테이블부터 순서대로 삭제)
     */
    @Override
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId));

        // [추가] 요청한 사용자가 프로젝트 방장인지 권한 검증
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!project.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        // 1단계: 해당 프로젝트의 게시글에 달린 댓글 먼저 삭제
        List<BoardPost> boardPosts = boardPostRepository.findAllByProjectId(projectId);
        for (BoardPost post : boardPosts) {
            commentRepository.deleteAll(
                    commentRepository.findAllByPostIdOrderByCreatedAtAsc(post.getId())
            );
        }

        // 2단계: 해당 프로젝트의 게시글 삭제
        boardPostRepository.deleteAllByProjectId(projectId);

        // 3단계: 해당 프로젝트의 지원서 삭제
        applicationRepository.deleteAllByProjectId(projectId);

        // 4단계: 해당 프로젝트의 팀원 삭제
        projectMemberRepository.deleteAllByProjectId(projectId);

        // 5단계: 프로젝트 삭제
        projectRepository.delete(project);
    }

    /**
     * 6. 프로젝트 모집 수동 마감
     */
    @Override
    public ProjectResponseDto closeProjectRecruitment(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId));

        // [추가] 요청한 사용자가 프로젝트 방장인지 권한 검증
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!project.getOwner().getId().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new BusinessException(ErrorCode.PROJECT_CLOSED);
        }
        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND, "삭제된 프로젝트는 마감할 수 없습니다.");
        }

        project.closeRecruitment();
        return ProjectMapper.mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getMyOwnedPosts(Long userId) {
        return projectRepository.findAllByOwnerId(userId).stream()
                .map(project -> ProjectMapper.mapToResponse(project, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getMyJoinedProjects(Long userId) {
        return applicationRepository.findAllByApplicantIdAndStatus(userId, ApplicationStatus.ACCEPTED)
                .stream()
                .map(app -> ProjectMapper.mapToResponse(app.getProject(), userId))
                .collect(Collectors.toList());
    }
}