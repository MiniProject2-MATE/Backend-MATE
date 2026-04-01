package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.ApplicationRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.entity.Application;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus; // Enum 경로 확인 필요
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
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
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, requestDto.getProjectId()));

        // 지원자 존재 여부 확인
        User applicant = userRepository.findById(requestDto.getApplicantId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, requestDto.getApplicantId()));

        // 이미 마감된 프로젝트인지 확인 (ErrorCode.PROJECT_CLOSED 활용)
        if (project.getStatus() == ProjectStatus.CLOSED) {
            throw new BusinessException(ErrorCode.PROJECT_CLOSED);
        }

        // 중복 지원 방지 (ErrorCode.APPLY_DUPLICATE 활용)
        // 주의: applicationRepository에 해당 유저가 이 프로젝트에 지원했는지 확인하는 메서드가 필요합니다. (예: existsByProjectIdAndApplicantId)
        if (applicationRepository.existsByProjectIdAndApplicantId(project.getId(), applicant.getId())) {
            throw new BusinessException(ErrorCode.APPLY_DUPLICATE);
        }

        // DTO -> Entity 변환 및 저장
        Application application = ApplicationMapper.mapToEntity(requestDto, project, applicant);
        Application savedApplication = applicationRepository.save(application);

        return ApplicationMapper.mapToApplicationResponse(savedApplication);
    }

    /**
     * 2. 특정 프로젝트의 지원자 목록 보기 (방장용)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId) {
        // 프로젝트가 진짜 있는지 먼저 검사하고 싶다면 아래 코드 추가
        if (!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, projectId);
        }

        return applicationRepository.findAllByProjectId(projectId).stream()
                .map(ApplicationMapper::mapToApplicationResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. 지원 취소 또는 거절 (삭제)
     */
    @Override
    public void deleteApplication(Long applicationId) {
        // 지원서 존재 여부 확인
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.APPLY_NOT_FOUND, applicationId));

        // 이미 승인/거절 처리된 지원서는 취소 불가 로직 (ErrorCode.APPLY_CANNOT_CANCEL 활용)
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new BusinessException(ErrorCode.APPLY_CANNOT_CANCEL);
        }

        applicationRepository.delete(application);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getMyPendingApplications(Long userId) {
        // 1. 내가 신청했고, 상태가 ACCEPTED가 아닌(Not) 것들을 조회 (PENDING, REJECTED)
        return applicationRepository.findAllByApplicantIdAndStatusNot(userId, ApplicationStatus.ACCEPTED)
                .stream()
                // 2. ApplicationResponseDto로 변환 (매퍼에서 projectTitle 포함됨)
                .map(ApplicationMapper::mapToApplicationResponse)
                .collect(Collectors.toList());
    }
}