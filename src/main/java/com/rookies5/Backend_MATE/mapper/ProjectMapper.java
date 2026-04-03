package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.dto.request.ProjectRequestDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class ProjectMapper {

    /**
     * 1. 기존 버전 (매개변수 1개): 상세 조회, 수정, 마감 등 기존 코드용
     * 사진에서 빨간 줄 났던 부분들을 이 메서드가 처리해줍니다.
     */
    public static ProjectResponseDto mapToResponse(Project project) {
        // 내부적으로 아래에 있는 2번 메서드를 호출하되, userId는 null로 넘깁니다.
        return mapToResponse(project, null);
    }

    /**
     * 2. 마이페이지 버전 (매개변수 2개): 내 모집글/참여글 조회용
     * currentUserId를 받아서 방장 여부(isOwner)와 역할(role)을 계산합니다.
     */
    public static ProjectResponseDto mapToResponse(Project project, Long currentUserId) {
        // 방장 여부 판별 (로그인한 유저 ID가 있고, 그게 방장 ID와 같을 때 true)
        boolean isOwner = Objects.equals(project.getOwner().getId(), currentUserId);

        return ProjectResponseDto.builder()
                .id(project.getId())
                .ownerId(project.getOwner() != null ? project.getOwner().getId() : null)
                .ownerNickname(project.getOwner() != null ? project.getOwner().getNickname() : "알 수 없음")
                .category(project.getCategory())
                .title(project.getTitle())
                .content(project.getContent())
                .recruitCount(project.getRecruitCount())
                .currentCount(project.getCurrentCount())
                .status(project.getStatus())
                .onOffline(project.getOnOffline())
                .endDate(project.getEndDate())

                .deleted(project.getDeletedAt()!=null)
                // D-Day 계산
                .remainingDays(project.getEndDate() != null ?
                        java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), project.getEndDate()) : null)
                // ★ 마이페이지 전용 필드 주입
                .isOwner(isOwner)
                .role(isOwner ? "OWNER" : "MEMBER")
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