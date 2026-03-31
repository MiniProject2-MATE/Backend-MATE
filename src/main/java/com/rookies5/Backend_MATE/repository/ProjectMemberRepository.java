package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
    // 프로젝트 ID로 팀원 목록 찾기
    List<ProjectMember> findAllByProjectId(Long projectId);
}