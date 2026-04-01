package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.dto.request.ProjectRequestDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ProjectMapper {

    // 1. Entity -> Response DTO (조회용: 방장 정보와 D-Day 포함)
    public static ProjectResponseDto mapToResponse(Project project) {
        return ProjectResponseDto.builder()
                .id(project.getId())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                // 방장 닉네임을 여기서 꺼내서 전달!
                .ownerNickname(project.getOwner() != null ? project.getOwner().getNickname() : "알 수 없음")
                .category(project.getCategory())
                .title(project.getTitle())
                .content(project.getContent())
                .recruitCount(project.getRecruitCount())
                .currentCount(project.getCurrentCount())
                .status(project.getStatus())
                .onOffline(project.getOnOffline())
                .endDate(project.getEndDate())
                // 오늘부터 마감일까지 남은 날짜 계산 (D-Day)
                .remainingDays(project.getEndDate() != null ? ChronoUnit.DAYS.between(LocalDate.now(), project.getEndDate()) : null)
                .build();
    }

    // 2. Request DTO -> Entity (저장용: 방장 엔티티와 조립)
    public static Project mapToEntity(ProjectRequestDto requestDto, User owner) {
        return Project.builder()
                .owner(owner) // 파라미터로 받은 실제 User 엔티티 연결
                .category(requestDto.getCategory())
                .title(requestDto.getTitle())
                .content(requestDto.getContent())
                .recruitCount(requestDto.getRecruitCount())
                // 방장 본인이 첫 번째 멤버이므로 보통 1로 시작하거나, 팀 규칙에 따라 0 세팅
                .currentCount(1)
                .status(ProjectStatus.RECRUITING) // 기본 상태: 모집 중
                .onOffline(requestDto.getOnOffline())
                .endDate(requestDto.getEndDate())
                .build();
    }
}