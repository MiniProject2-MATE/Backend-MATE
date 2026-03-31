package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
    // 프로젝트 ID로 게시글 찾기 + 작성일 최신순 정렬
    List<BoardPost> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);
}