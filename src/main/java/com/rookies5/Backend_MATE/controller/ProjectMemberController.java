package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.response.ProjectMemberResponseDto;
import com.rookies5.Backend_MATE.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /**
     * 특정 프로젝트에 참여 중인 모든 멤버 목록 조회
     */
    @GetMapping("/{projectId}/members")
    public SuccessResponse<List<ProjectMemberResponseDto>> getMembersByProjectId(@PathVariable Long projectId) {
        log.info("프로젝트 멤버 목록 조회 요청 - projectId: {}", projectId);
        List<ProjectMemberResponseDto> responseDtoList = projectMemberService.getMembersByProjectId(projectId);
        return new SuccessResponse<>("프로젝트 멤버 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 프로젝트에서 특정 멤버를 제외 (추방 또는 탈퇴)
     */
    @DeleteMapping("/members/{memberId}")
    public SuccessResponse<Void> removeMember(@PathVariable Long memberId) {
        log.info("프로젝트 멤버 제외 요청 - memberId: {}", memberId);
        projectMemberService.removeMember(memberId);
        return new SuccessResponse<>("멤버가 성공적으로 제외되었습니다.");
    }
}