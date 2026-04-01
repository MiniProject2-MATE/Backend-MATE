package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.BoardPostRequestDto;
import com.rookies5.Backend_MATE.dto.response.BoardPostResponseDto;
import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;

import com.rookies5.Backend_MATE.mapper.BoardPostMapper;
import com.rookies5.Backend_MATE.repository.BoardPostRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.service.BoardPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rookies5.Backend_MATE.repository.ProjectMemberRepository;
import com.rookies5.Backend_MATE.exception.AccessDeniedException;
import com.rookies5.Backend_MATE.exception.InvalidRequestException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardPostServiceImpl implements BoardPostService {

    private final BoardPostRepository boardPostRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository; // 추가: 권한 검증용

    /**
     * 새로운 게시글 작성
     */
    @Override
    public BoardPostResponseDto createPost(BoardPostRequestDto requestDto) {
        // 프로젝트 존재 여부 확인 예외처리
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, requestDto.getProjectId()));

        // 작성자 존재 여부 확인 예외처리
        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, requestDto.getAuthorId()));

        // DTO -> Entity 변환 및 저장
        BoardPost post = BoardPostMapper.mapToEntity(requestDto, project, author);
        BoardPost savedPost = boardPostRepository.save(post);

        // Entity -> Response DTO 반환
        return BoardPostMapper.mapToResponse(savedPost);
    }

    /**
     * 특정 프로젝트의 모든 게시글 조회 (최신순)
     */
    @Transactional(readOnly = true)
    @Override
    public List<BoardPostResponseDto> getPostsByProjectId(Long projectId) {
        return boardPostRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(BoardPostMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 수정
     */
    @Override
    public BoardPostResponseDto updatePost(Long postId, BoardPostRequestDto requestDto) {
        // 게시글 존재 여부 확인 예외처리
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, postId));

        // 추후 추가할 비즈니스 로직: 요청한 사용자가 실제 작성자인지 권한 검증
        // if (!post.getAuthor().getId().equals(requestDto.getAuthorId())) {
        //     throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "게시글 수정 권한이 없습니다.");
        // }

        // 엔티티 내부의 업데이트 로직 호출
        post.updatePost(requestDto.getTitle(), requestDto.getContent());

        return BoardPostMapper.mapToResponse(post);
    }

    /**
     * 게시글 삭제
     */
    @Override
    public void deletePost(Long postId) {
        // 게시글 존재 여부 확인 예외처리
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, postId));

        // 추후 추가할 비즈니스 로직: 요청한 사용자가 실제 작성자이거나 방장인지 권한 검증
        // if (!post.getAuthor().getId().equals(currentUserId)) {
        //     throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED, "게시글 삭제 권한이 없습니다.");
        // }

        boardPostRepository.delete(post);
    }
    /**
     * 게시글 상세 조회 (권한 검증 및 조회수 증가 로직 포함)
     */
    @Override
    public BoardPostResponseDto getPostDetail(Long projectId, Long postId, Long userId) {
        // 1. 게시글 존재 여부 확인
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, postId));

        // 2. 해당 게시글이 요청한 프로젝트에 속하는지 검증
        if (!post.getProject().getId().equals(projectId)) {
            throw new InvalidRequestException(ErrorCode.VALIDATION_ERROR, "해당 프로젝트의 게시글이 아닙니다.");
        }

        // 3. 권한 검증: 해당 프로젝트의 팀원(MEMBER, OWNER)인지 확인
        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(projectId, userId);
        if (!isMember) {
            throw new AccessDeniedException(ErrorCode.AUTH_ACCESS_DENIED, "프로젝트 팀원만 게시글을 조회할 수 있습니다.");
        }

        // 4. 조회수 증가 로직
        // (무한 새로고침 방지는 추후 Redis/Cookie로 고도화 가능, 현재는 단순 1 증가)
        post.incrementViewCount();

        // 5. DTO 변환 후 반환 (기존 BoardPostMapper.mapToResponse 그대로 활용)
        return BoardPostMapper.mapToResponse(post);
    }
}