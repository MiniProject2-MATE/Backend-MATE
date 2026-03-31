package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.ApplicationRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.entity.Application;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.mapper.ApplicationMapper;
import com.rookies5.Backend_MATE.repository.ApplicationRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /**
     * 1. 프로젝트 지원하기
     */
    @Override
    public ApplicationResponseDto applyToProject(ApplicationRequestDto requestDto) {
        // 프로젝트 존재 여부 확인
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // 지원자 존재 여부 확인
        User applicant = userRepository.findById(requestDto.getApplicantId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // DTO -> Entity 변환 및 저장
        Application application = ApplicationMapper.mapToEntity(requestDto, project, applicant);
        Application savedApplication = applicationRepository.save(application);

        // Entity -> Response DTO 변환 후 반환
        return ApplicationMapper.mapToResponse(savedApplication);
    }

    /**
     * 2. 특정 프로젝트의 지원자 목록 보기 (방장용)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId) {
        return applicationRepository.findAllByProjectId(projectId).stream()
                .map(ApplicationMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. 지원 취소 또는 거절 (삭제)
     */
    @Override
    public void deleteApplication(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        applicationRepository.delete(application);
    }
}