package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.ProjectRequestDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 새로운 프로젝트 모집글 생성
     */
    @PostMapping
    public SuccessResponse<ProjectResponseDto> createProject(@Valid @RequestBody ProjectRequestDto requestDto) {
        log.info("프로젝트 생성 요청 - ownerId: {}, title: {}", requestDto.getOwnerId(), requestDto.getTitle());
        ProjectResponseDto responseDto = projectService.createProject(requestDto);
        return new SuccessResponse<>("프로젝트가 성공적으로 생성되었습니다.", responseDto);
    }

    /**
     * 전체 프로젝트 목록 조회
     */
    @GetMapping
    public SuccessResponse<List<ProjectResponseDto>> getAllProjects() {
        log.info("전체 프로젝트 목록 조회 요청");
        List<ProjectResponseDto> responseDtoList = projectService.getAllProjects();
        return new SuccessResponse<>("프로젝트 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 특정 프로젝트 상세 조회
     */
    @GetMapping("/{projectId}")
    public SuccessResponse<ProjectResponseDto> getProjectById(@PathVariable Long projectId) {
        log.info("프로젝트 상세 조회 요청 - projectId: {}", projectId);
        ProjectResponseDto responseDto = projectService.getProjectById(projectId);
        return new SuccessResponse<>("프로젝트 상세 조회가 완료되었습니다.", responseDto);
    }

    /**
     * 프로젝트 정보 수정
     */
    @PutMapping("/{projectId}")
    public SuccessResponse<ProjectResponseDto> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectRequestDto requestDto) {
        log.info("프로젝트 수정 요청 - projectId: {}", projectId);
        ProjectResponseDto responseDto = projectService.updateProject(projectId, requestDto);
        return new SuccessResponse<>("프로젝트가 성공적으로 수정되었습니다.", responseDto);
    }

    /**
     * 프로젝트 삭제
     */
    @DeleteMapping("/{projectId}")
    public SuccessResponse<Void> deleteProject(@PathVariable Long projectId) {
        log.info("프로젝트 삭제 요청 - projectId: {}", projectId);
        projectService.deleteProject(projectId);
        return new SuccessResponse<>("프로젝트가 성공적으로 삭제되었습니다.");
    }

    /**
     * 프로젝트 수동 마감
     */
    @PatchMapping("/{projectId}/close")
    public SuccessResponse<ProjectResponseDto> closeProjectRecruitment(@PathVariable Long projectId) {
        log.info("프로젝트 수동 마감 요청 - projectId: {}", projectId);
        ProjectResponseDto responseDto = projectService.closeProjectRecruitment(projectId);
        return new SuccessResponse<>("프로젝트 모집이 마감되었습니다.", responseDto);
    }
}