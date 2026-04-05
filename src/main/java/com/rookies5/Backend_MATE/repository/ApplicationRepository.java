package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.Application;
import com.rookies5.Backend_MATE.entity.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    // 프로젝트 ID로 지원서 목록 찾기
    List<Application> findAllByProjectId(Long projectId);

    boolean existsByProjectIdAndApplicantId(Long projectId, Long applicantId);

    // 내 프로젝트/스터디 조회: 내가 신청했고, 상태가 ACCEPTED인 것만
    List<Application> findAllByApplicantIdAndStatus(Long applicantId, ApplicationStatus status);

    // 내 신청 현황 조회: 내가 신청했고, 상태가 ACCEPTED가 아닌 것들 (PENDING, REJECTED)
    List<Application> findAllByApplicantIdAndStatusNot(Long applicantId, ApplicationStatus status);

    //프로젝트 삭제 -> 지원서 삭제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Application a SET a.deletedAt = CURRENT_TIMESTAMP " +
            "WHERE a.project.id = :projectId AND a.deletedAt IS NULL")
    void softDeleteAllByProjectId(@Param("projectId") Long projectId);

    //회원 탈퇴 -> 지원서 삭제
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Application a SET a.deletedAt = CURRENT_TIMESTAMP WHERE a.applicant.id = :userId AND a.deletedAt IS NULL")
    void softDeleteAllByApplicantId(@Param("userId") Long userId);
}