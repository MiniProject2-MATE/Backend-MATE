package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.ProjectRequestDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
import com.rookies5.Backend_MATE.repository.ApplicationRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
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

    /**
     * 1. 프로젝트 생성
     */
    @Override
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {
        // 방장(User) 엔티티 존재 여부 확인 예외처리
        User owner = userRepository.findById(requestDto.getOwnerId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, requestDto.getOwnerId()));

        // DTO -> Entity 변환
        Project project = ProjectMapper.mapToEntity(requestDto, owner);

        // 저장 처리
        Project savedProject = projectRepository.save(project);

        // Entity -> Response DTO 변환 후 리턴
        return ProjectMapper.mapToResponse(savedProject);
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
     * 4. 수정 로직
     */
    @Override
    public ProjectResponseDto updateProject(Long projectId, ProjectRequestDto requestDto) {
        // 프로젝트 존재 여부 확인 예외처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId));

        // 추후 추가할 비즈니스 로직: 요청한 사용자가 프로젝트 방장인지 권한 검증
        // if (!project.getOwner().getId().equals(currentUserId)) {
        //     throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "프로젝트 수정 권한이 없습니다.");
        // }

        // Dirty Checking: 엔티티 내부 메서드 호출하여 정보 업데이트
        project.updateProject(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getRecruitCount(),
                requestDto.getEndDate(),
                requestDto.getOnOffline(),
                requestDto.getStatus()
        );

        return ProjectMapper.mapToResponse(project);
    }

    /**
     * 5. 삭제 로직
     */
    @Override
    public void deleteProject(Long projectId) {
        // 프로젝트 존재 여부 확인 예외처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId));

        // 추후 추가할 비즈니스 로직: 요청한 사용자가 프로젝트 방장인지 권한 검증
        // if (!project.getOwner().getId().equals(currentUserId)) {
        //     throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "프로젝트 삭제 권한이 없습니다.");
        // }

        // 비즈니스 로직: 진행 중인 팀원이 있는 프로젝트는 삭제 불가 방어 로직 (선택사항)
        // if (project.getCurrentCount() > 0) {
        //     throw new BusinessException(ErrorCode.PROJECT_HAS_MEMBERS);
        // }

        projectRepository.delete(project);
    }

    /**
     * 6. 프로젝트 모집 수동 마감
     */
    @Override
    public ProjectResponseDto closeProjectRecruitment(Long projectId) {
        // 프로젝트 존재 여부 확인 예외처리
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId));

        // 추후 추가할 비즈니스 로직: 요청한 사용자가 프로젝트 방장인지 권한 검증
        // if (!project.getOwner().getId().equals(currentUserId)) {
        //     throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "프로젝트 마감 권한이 없습니다.");
        // }

        // 비즈니스 로직: 이미 마감되었거나 삭제된 프로젝트인지 체크
        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new BusinessException(ErrorCode.PROJECT_CLOSED);
        }
        if (project.getStatus() == ProjectStatus.DELETED) {
            throw new BusinessException(ErrorCode.PROJECT_NOT_FOUND, "삭제된 프로젝트는 마감할 수 없습니다.");
        }

        // 엔티티 상태 변경
        project.closeRecruitment();

        return ProjectMapper.mapToResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getMyOwnedPosts(Long userId) {
        // 1. 내가 owner_id인 프로젝트들을 레포지토리에서 가져옴
        return projectRepository.findAllByOwnerId(userId).stream()
                // 2. 매퍼를 통해 DTO로 변환 (isOwner=true 로직 포함)
                .map(project -> ProjectMapper.mapToResponse(project, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getMyJoinedProjects(Long userId) {
        // 1. Application 테이블에서 내가 신청(applicant)했고 승인(ACCEPTED)된 내역 조회
        return applicationRepository.findAllByApplicantIdAndStatus(userId, ApplicationStatus.ACCEPTED)
                .stream()
                // 2. 지원 내역에 연결된 'Project' 엔티티를 꺼내서 DTO로 변환
                .map(app -> ProjectMapper.mapToResponse(app.getProject(), userId))
                .collect(Collectors.toList());
    }
}