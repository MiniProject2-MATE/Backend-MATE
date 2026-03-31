package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.ProjectMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {
}