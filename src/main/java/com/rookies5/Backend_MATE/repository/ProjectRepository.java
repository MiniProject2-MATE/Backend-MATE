package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 작성자(Owner)의 ID로 프로젝트 목록을 찾는 메서드 정의
    List<Project> findAllByOwnerId(Long ownerId);
}