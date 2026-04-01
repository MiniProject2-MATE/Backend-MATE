package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // List를 사용하기 위해 추가

public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 특정 사용자(Owner)가 생성한 모든 프로젝트 목록을 조회합니다.
     * @param ownerId 프로젝트 생성자의 ID
     * @return 해당 사용자가 소유한 프로젝트 리스트
     */
    List<Project> findAllByOwnerId(Long ownerId);

}