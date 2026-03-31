package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.entity.*;
import com.rookies5.Backend_MATE.mapper.BoardPostMapper;
import com.rookies5.Backend_MATE.repository.*;
import com.rookies5.Backend_MATE.service.BoardPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardPostServiceImpl implements BoardPostService {
    private final BoardPostRepository boardPostRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Override
    public BoardPostDto createPost(BoardPostDto dto) {
        Project project = projectRepository.findById(dto.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        User author = userRepository.findById(dto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BoardPost post = BoardPostMapper.mapToBoardPost(dto, project, author);
        BoardPost savedPost = boardPostRepository.save(post);
        return BoardPostMapper.mapToBoardPostDto(savedPost);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BoardPostDto> getPostsByProjectId(Long projectId) {
        return boardPostRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(BoardPostMapper::mapToBoardPostDto)
                .toList();
    }

    @Override
    public BoardPostDto updatePost(Long postId, BoardPostDto dto) {
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        post.updatePost(dto.getTitle(), dto.getContent());

        return BoardPostMapper.mapToBoardPostDto(post);
    }

    @Override
    public void deletePost(Long postId) {
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        boardPostRepository.delete(post);
    }
}