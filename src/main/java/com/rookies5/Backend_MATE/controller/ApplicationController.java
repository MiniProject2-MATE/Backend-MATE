package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.ApplicationRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 1. 프로젝트 지원하기
     * POST /api/applications
     */
    @PostMapping
    public SuccessResponse<ApplicationResponseDto> applyToProject(
            @Valid @RequestBody ApplicationRequestDto requestDto) {
        log.info("프로젝트 지원 요청 - projectId: {}, applicantId: {}",
                requestDto.getProjectId(), requestDto.getApplicantId());
        ApplicationResponseDto responseDto = applicationService.applyToProject(requestDto);
        return new SuccessResponse<>("지원이 완료되었습니다.", responseDto);
    }

    /**
     * 2. 특정 프로젝트의 지원자 목록 조회 (방장용)
     * GET /api/applications/projects/{projectId}
     */
    @GetMapping("/projects/{projectId}")
    public SuccessResponse<List<ApplicationResponseDto>> getApplicationsByProjectId(
            @PathVariable Long projectId) {
        log.info("프로젝트 지원자 목록 조회 요청 - projectId: {}", projectId);
        List<ApplicationResponseDto> responseDtoList = applicationService.getApplicationsByProjectId(projectId);
        return new SuccessResponse<>("지원자 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 3. 지원 취소 (PENDING 상태일 때만 가능)
     * DELETE /api/applications/{applicationId}
     */
    @DeleteMapping("/{applicationId}")
    public SuccessResponse<Void> deleteApplication(@PathVariable Long applicationId) {
        log.info("지원 취소 요청 - applicationId: {}", applicationId);
        applicationService.deleteApplication(applicationId);
        return new SuccessResponse<>("지원이 취소되었습니다.");
    }

    /**
     * 4. 지원서 승인 (방장용)
     * PATCH /api/applications/{applicationId}/accept
     */
    @PatchMapping("/{applicationId}/accept")
    public SuccessResponse<ApplicationResponseDto> acceptApplication(@PathVariable Long applicationId) {
        log.info("지원서 승인 요청 - applicationId: {}", applicationId);
        ApplicationResponseDto responseDto = applicationService.acceptApplication(applicationId);
        return new SuccessResponse<>("지원서가 승인되었습니다.", responseDto);
    }

    /**
     * 5. 지원서 거절 (방장용)
     * PATCH /api/applications/{applicationId}/reject
     */
    @PatchMapping("/{applicationId}/reject")
    public SuccessResponse<ApplicationResponseDto> rejectApplication(@PathVariable Long applicationId) {
        log.info("지원서 거절 요청 - applicationId: {}", applicationId);
        ApplicationResponseDto responseDto = applicationService.rejectApplication(applicationId);
        return new SuccessResponse<>("지원서가 거절되었습니다.", responseDto);
    }
}