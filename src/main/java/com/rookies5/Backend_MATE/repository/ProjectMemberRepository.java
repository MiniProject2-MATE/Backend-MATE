package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    // 프로젝트 ID로 팀원 목록 찾기
    List<ProjectMember> findAllByProjectId(Long projectId);

    boolean existsByProjectIdAndUserId(Long projectId, Long userId);

    // 프로젝트 삭제 시 연관된 팀원 전체 삭제 (FK 제약 조건 해결)
    void deleteAllByProjectId(Long projectId);
}