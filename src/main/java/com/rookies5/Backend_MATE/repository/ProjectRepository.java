package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}