package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.ProjectDto;
import java.util.List;

public interface ProjectService {
    ProjectDto createProject(ProjectDto projectDto);
    ProjectDto getProjectById(Long projectId);
    List<ProjectDto> getAllProjects();
    ProjectDto updateProject(Long projectId, ProjectDto updatedProject);
    void deleteProject(Long projectId);
}