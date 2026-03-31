package com.rookies5.Backend_MATE.entity;

import com.rookies5.Backend_MATE.entity.BaseEntity;
import com.rookies5.Backend_MATE.entity.enums.Category;
import com.rookies5.Backend_MATE.entity.enums.OnOffline;
import com.rookies5.Backend_MATE.entity.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.Where;
import java.time.LocalDate;

@Entity
@Table(name = "projects", indexes = {@Index(name = "idx_status", columnList = "status")})
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Project extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
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

    @Column(name = "current_count", nullable = false)
    private Integer currentCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "on_offline", nullable = false, length = 20)
    private OnOffline onOffline;

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
}