package com.rookies5.Backend_MATE.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;

    @NotNull(message = "게시글 ID는 필수입니다.")
    private Long postId;

    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long authorId;

    // 프론트엔드 표시용 데이터 (화면에 닉네임과 프사를 띄워주기 위함)
    private String authorNickname;
    private String authorProfileImg;

    @NotBlank(message = "댓글 내용은 필수 입력 항목입니다.")
    private String content;

    private LocalDateTime createdAt;
}