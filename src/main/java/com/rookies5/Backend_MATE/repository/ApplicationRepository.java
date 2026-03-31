package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
}