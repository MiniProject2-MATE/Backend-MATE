package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.BoardPost;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardPostRepository extends JpaRepository<BoardPost, Long> {
}