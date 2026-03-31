package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.entity.*;
import com.rookies5.Backend_MATE.mapper.ApplicationMapper;
import com.rookies5.Backend_MATE.repository.*;
import com.rookies5.Backend_MATE.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // 1. 지원하기
    @Override
    public ApplicationDto applyToProject(ApplicationDto applicationDto) {
        Project project = projectRepository.findById(applicationDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User applicant = userRepository.findById(applicationDto.getApplicantId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Application application = ApplicationMapper.mapToApplication(applicationDto, project, applicant);
        Application savedApplication = applicationRepository.save(application);
        
        return ApplicationMapper.mapToApplicationDto(savedApplication);
    }

    // 2. 특정 프로젝트의 지원자 목록 보기 (방장용)
    @Transactional(readOnly = true)
    @Override
    public List<ApplicationDto> getApplicationsByProjectId(Long projectId) {
        return applicationRepository.findAllByProjectId(projectId).stream()
                .map(ApplicationMapper::mapToApplicationDto)
                .toList();
    }

    // 3. 지원 취소 또는 거절 (삭제)
    @Override
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        applicationRepository.delete(application);
    }
}