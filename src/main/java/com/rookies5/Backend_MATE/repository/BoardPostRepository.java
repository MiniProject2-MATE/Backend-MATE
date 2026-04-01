package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    // 프로젝트 ID로 게시글 찾기 + 작성일 최신순 정렬
    List<BoardPost> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

    // 프로젝트 ID로 게시글 ID 목록 조회 (댓글 삭제용)
    List<BoardPost> findAllByProjectId(Long projectId);

    // 프로젝트 삭제 시 연관된 게시글 전체 삭제 (FK 제약 조건 해결)
    // @Where soft delete 필터를 우회하기 위해 JPQL 사용
    @Modifying
    @Query("DELETE FROM BoardPost bp WHERE bp.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") Long projectId);
}