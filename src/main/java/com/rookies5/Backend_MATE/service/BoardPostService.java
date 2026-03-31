package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.BoardPostDto;
import java.util.List;

public interface BoardPostService {
    BoardPostDto createPost(BoardPostDto dto);
    List<BoardPostDto> getPostsByProjectId(Long projectId);
    BoardPostDto updatePost(Long postId, BoardPostDto dto);
    void deletePost(Long postId);
}