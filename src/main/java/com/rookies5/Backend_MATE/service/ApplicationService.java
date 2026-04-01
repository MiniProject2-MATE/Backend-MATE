package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.ApplicationRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApplicationService {

    /**
     * 프로젝트 지원하기
     * @param requestDto 지원 정보 (메시지 등)
     * @return 생성된 지원서 정보
     */
    ApplicationResponseDto applyToProject(ApplicationRequestDto requestDto);

    /**
     * 특정 프로젝트의 지원자 목록 조회 (방장용)
     * @param projectId 프로젝트 ID
     * @return 지원서 리스트 (지원자 닉네임, 포지션 포함)
     */
    List<ApplicationResponseDto> getApplicationsByProjectId(Long projectId);

    /**
     * 지원 취소하기
     * @param applicationId 지원서 ID
     */
    void deleteApplication(Long applicationId);

    List<ApplicationResponseDto> getMyPendingApplications(Long userId); // 내 신청 현황

}