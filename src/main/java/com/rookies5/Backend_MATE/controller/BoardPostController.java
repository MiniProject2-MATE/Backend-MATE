package com.rookies5.Backend_MATE.controller;

import com.rookies5.Backend_MATE.common.SuccessResponse;
import com.rookies5.Backend_MATE.dto.request.BoardPostRequestDto;
import com.rookies5.Backend_MATE.dto.response.BoardPostResponseDto;
import com.rookies5.Backend_MATE.service.BoardPostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class BoardPostController {

    private final BoardPostService boardPostService;

    /**
     * 새로운 게시글 작성
     */
    @PostMapping
    public SuccessResponse<BoardPostResponseDto> createPost(@Valid @RequestBody BoardPostRequestDto requestDto) {
        log.info("게시글 작성 요청 - projectId: {}, authorId: {}", requestDto.getProjectId(), requestDto.getAuthorId());
        BoardPostResponseDto responseDto = boardPostService.createPost(requestDto);
        return new SuccessResponse<>("게시글이 성공적으로 작성되었습니다.", responseDto);
    }

    /**
     * 특정 프로젝트의 모든 게시글 조회
     */
    @GetMapping("/{projectId}/board")
    public SuccessResponse<List<BoardPostResponseDto>> getPostsByProjectId(@PathVariable Long projectId) {
        log.info("프로젝트 게시글 목록 조회 요청 - projectId: {}", projectId);
        List<BoardPostResponseDto> responseDtoList = boardPostService.getPostsByProjectId(projectId);
        return new SuccessResponse<>("게시글 목록 조회가 완료되었습니다.", responseDtoList);
    }

    /**
     * 특정 프로젝트의 게시글 상세 조회 (조회수 증가 및 권한 검증 포함)
     */
    @GetMapping("/{projectId}/board/{postId}")
    public SuccessResponse<BoardPostResponseDto> getPostDetail(
            @PathVariable Long projectId,
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId) { // 테스트를 위해 헤더로 유저 ID를 받음
        log.info("게시글 상세 조회 요청 - projectId: {}, postId: {}, userId: {}", projectId, postId, userId);
        BoardPostResponseDto responseDto = boardPostService.getPostDetail(projectId, postId, userId);
        return new SuccessResponse<>("게시글 조회가 완료되었습니다.", responseDto);
    }

    /**
     * 기존 게시글 수정
     */
    @PutMapping("/{postId}")
    public SuccessResponse<BoardPostResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody BoardPostRequestDto requestDto) {
        log.info("게시글 수정 요청 - postId: {}", postId);
        BoardPostResponseDto responseDto = boardPostService.updatePost(postId, requestDto);
        return new SuccessResponse<>("게시글이 성공적으로 수정되었습니다.", responseDto);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{postId}")
    public SuccessResponse<Void> deletePost(@PathVariable Long postId) {
        log.info("게시글 삭제 요청 - postId: {}", postId);
        boardPostService.deletePost(postId);
        return new SuccessResponse<>("게시글이 성공적으로 삭제되었습니다.");
    }
}