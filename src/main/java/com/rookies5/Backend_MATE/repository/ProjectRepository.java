package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // 작성자(Owner)의 ID로 프로젝트 목록을 찾는 메서드 정의
    @Query("SELECT p FROM Project p WHERE p.deletedAt IS NULL AND p.owner.id = :ownerId")
    List<Project> findAllByOwnerId(Long ownerId);
    //프로젝트 삭제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Project p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.id = :projectId")
    void softDeleteById(@Param("projectId") Long projectId);

    //회원 탈퇴 -> 프로젝트 삭제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Project p SET p.deletedAt = CURRENT_TIMESTAMP WHERE p.owner.id = :ownerId AND p.deletedAt IS NULL")
    void softDeleteAllByOwnerId(@Param("ownerId") Long ownerId);
    //관리자용
    @Query(value = "SELECT * FROM projects ORDER BY created_at DESC",
            countQuery = "SELECT count(*) FROM projects",
            nativeQuery = true)
    Page<Project> findAllIncludingDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM projects WHERE project_id = :id", nativeQuery = true)
    Optional<Project> findByIdIncludingDeleted(@Param("id") Long id);

    @Query(value = "SELECT count(*) FROM projects", nativeQuery = true)
    long countIncludingDeleted();

    @Query(value = "SELECT * FROM projects", nativeQuery = true)
    List<Project> findAllIncludingDeletedList();
}