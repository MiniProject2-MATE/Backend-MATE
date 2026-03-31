package com.rookies5.Backend_MATE.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "board_posts")
@Where(clause = "deleted_at IS NULL")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class BoardPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    @NotBlank(message = "내용은 필수입니다")
    private String content;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    public void incrementViewCount() { this.viewCount++; }

    public void updatePost(String title, String content) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
    }
}