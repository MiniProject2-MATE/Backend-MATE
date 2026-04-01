package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // 프로젝트 ID로 지원서 목록 찾기
    List<Application> findAllByProjectId(Long projectId);

    boolean existsByProjectIdAndApplicantId(Long projectId, Long applicantId);

    // 지원자(Applicant)의 ID로 지원 내역을 찾는 메서드 정의
    List<Application> findAllByUserId(Long applicantId);

}