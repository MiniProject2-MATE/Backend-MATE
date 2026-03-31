package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.BoardPostRequestDto;
import com.rookies5.Backend_MATE.dto.response.BoardPostResponseDto;

import java.util.List;

public interface BoardPostService {

    /**
     * 새로운 게시글 작성
     * @param requestDto 게시글 제목, 내용 등 작성 정보
     * @return 생성된 게시글 상세 정보 (ID, 작성자 닉네임 등 포함)
     */
    BoardPostResponseDto createPost(BoardPostRequestDto requestDto);

    /**
     * 특정 프로젝트에 속한 모든 게시글 조회
     * @param projectId 프로젝트 ID
     * @return 해당 프로젝트의 게시글 리스트
     */
    List<BoardPostResponseDto> getPostsByProjectId(Long projectId);

    /**
     * 기존 게시글 수정
     * @param postId 수정할 게시글 ID
     * @param requestDto 수정할 내용 (제목, 내용 등)
     * @return 수정이 완료된 게시글 정보
     */
    BoardPostResponseDto updatePost(Long postId, BoardPostRequestDto requestDto);

    /**
     * 게시글 삭제
     * @param postId 삭제할 게시글 ID
     */
    void deletePost(Long postId);
}