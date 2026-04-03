package com.rookies5.Backend_MATE.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rookies5.Backend_MATE.entity.BaseEntity;
import com.rookies5.Backend_MATE.entity.enums.Category;
import com.rookies5.Backend_MATE.entity.enums.OnOffline;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Where;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects", indexes = {@Index(name = "idx_status", columnList = "status")})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonIgnoreProperties({"myProjects"})
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Category category;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "제목은 필수입니다")
    @Size(min = 5, max = 50, message = "제목은 5~50자 이내여야 합니다")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @Column(name = "recruit_count", nullable = false)
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다")
    @Max(value = 20, message = "모집 인원은 20명을 넘을 수 없습니다")
    private Integer recruitCount;

    @Builder.Default
    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "on_offline", nullable = false, length = 20)
    private OnOffline onOffline;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.RECRUITING;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    public void addMember() {
        if (this.currentCount >= this.recruitCount) {
            throw new IllegalStateException("모집 정원이 가득 찼습니다.");
        }
        this.currentCount++;
        if (this.currentCount.equals(this.recruitCount)) {
            this.status = ProjectStatus.CLOSED; // 정원 충족 시 상태 자동 마감
        }
    }

    // 모집 수동 마감
    public void closeRecruitment() {
        this.status = ProjectStatus.CLOSED;
    }

    // 인원수 감소 로직 추가
    public void decreaseCurrentCount() {
        if (this.currentCount > 0) {
            this.currentCount--;
        }
    }

    public void updateProject(String title, String content, Integer recruitCount,
                              LocalDate endDate, OnOffline onOffline, ProjectStatus status) { // 👈 파라미터 추가
        this.title = title;
        this.content = content;
        this.recruitCount = recruitCount;
        this.endDate = endDate;
        this.onOffline = onOffline;

        // status가 null이 아닐 때만 업데이트
        if (status != null) {
            this.status = status;
        }
    }
    public boolean isDeleted() {
        return this.getDeletedAt() != null;
    }
    //soft delete 추가
    public void softDelete() {
        this.setDeletedAt(LocalDateTime.now());
    }
}