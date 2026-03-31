package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.ProjectRequestDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
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

    /**
     * 1. 프로젝트 생성
     */
    @Override
    public ProjectResponseDto createProject(ProjectRequestDto requestDto) {
        // 방장(User) 엔티티 존재 여부 확인
        User owner = userRepository.findById(requestDto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + requestDto.getOwnerId()));

        // DTO -> Entity 변환 (Static 매퍼 사용)
        Project project = ProjectMapper.mapToEntity(requestDto, owner);

        // 저장 처리
        Project savedProject = projectRepository.save(project);

        // Entity -> Response DTO 변환 후 리턴 (이때 D-Day와 닉네임이 계산되어 들어감)
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
                .orElseThrow(() -> new RuntimeException("Project does not exist with id: " + projectId));
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
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project does not exist with id: " + projectId));

        // Dirty Checking: 엔티티 내부 메서드 호출하여 정보 업데이트
        project.updateProject(
                requestDto.getTitle(),
                requestDto.getContent(),
                requestDto.getRecruitCount(),
                requestDto.getEndDate(),
                requestDto.getOnOffline()
        );

        // 변경된 내용을 다시 Response DTO로 변환하여 리턴
        return ProjectMapper.mapToResponse(project);
    }

    /**
     * 5. 삭제 로직
     */
    @Override
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project does not exist with id: " + projectId));

        projectRepository.delete(project);
    }
}