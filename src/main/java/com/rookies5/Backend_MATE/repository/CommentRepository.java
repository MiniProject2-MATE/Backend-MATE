package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}