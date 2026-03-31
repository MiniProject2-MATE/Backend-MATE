package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.ProjectDto;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {
    
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository; // 방장 정보를 찾기 위해 추가

    // 1. 생성 로직
    @Override
    public ProjectDto createProject(ProjectDto projectDto) {
        // 1. 방장(User) 엔티티 먼저 찾기 (Department와 달리 연관관계가 있어서 필요합니다)
        User owner = userRepository.findById(projectDto.getOwnerId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + projectDto.getOwnerId()));

        // 2. DTO => Entity 변환 (Static 매퍼 사용!)
        Project project = ProjectMapper.mapToProject(projectDto, owner);
        
        // 3. 등록 처리
        Project savedProject = projectRepository.save(project);
        
        // 4. 등록된 Entity => DTO 변환 후 리턴
        return ProjectMapper.mapToProjectDto(savedProject);
    }

    // 2. 단건 조회 로직
    @Transactional(readOnly = true)
    @Override
    public ProjectDto getProjectById(Long projectId) {
        return projectRepository.findById(projectId) // Optional<Project>
                .map(ProjectMapper::mapToProjectDto) // Optional<ProjectDto>
                .orElseThrow(() -> new RuntimeException("Project is not exists with a given id: " + projectId));
    }

    // 3. 전체 목록 조회 로직
    @Transactional(readOnly = true)
    @Override
    public List<ProjectDto> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        
        return projects.stream() // Stream<Project>
                .map(ProjectMapper::mapToProjectDto) // Stream<ProjectDto>
                .toList(); // List<ProjectDto>
    }

    // 4. 수정 로직
    @Override
    public ProjectDto updateProject(Long projectId, ProjectDto updatedProject) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project is not exists with a given id: " + projectId));
        
        // Dirty Checking (Setter 또는 엔티티 내부 메서드 호출)
        project.updateProject(
                updatedProject.getTitle(),
                updatedProject.getContent(),
                updatedProject.getRecruitCount(),
                updatedProject.getEndDate(),
                updatedProject.getOnOffline() // 진행 방식도 DTO에 있다면 같이 넣어주세요!
        );

        // Entity => DTO 로 변환
        return ProjectMapper.mapToProjectDto(project);
    }

    // 5. 삭제 로직
    @Override
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project is not exists with a given id: " + projectId));
        
        projectRepository.delete(project);
    }
}