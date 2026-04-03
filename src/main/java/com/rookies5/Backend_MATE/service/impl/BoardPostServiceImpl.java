package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.BoardPostRequestDto;
import com.rookies5.Backend_MATE.dto.response.BoardPostResponseDto;
import com.rookies5.Backend_MATE.entity.BoardPost;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.*;

import com.rookies5.Backend_MATE.mapper.BoardPostMapper;
import com.rookies5.Backend_MATE.repository.BoardPostRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
import com.rookies5.Backend_MATE.service.BoardPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.rookies5.Backend_MATE.repository.ProjectMemberRepository;

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
    public BoardPostResponseDto createPost(Long userId, BoardPostRequestDto requestDto) {
        Project project = projectRepository.findById(requestDto.getProjectId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.PROJECT_NOT_FOUND, requestDto.getProjectId()));

        // 수정: authorId 대신 토큰에서 넘어온 userId로 유저 조회
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        BoardPost post = BoardPostMapper.mapToEntity(requestDto, project, author);
        BoardPost savedPost = boardPostRepository.save(post);

        // 수정: userId를 함께 넘겨줍니다.
        return BoardPostMapper.mapToResponse(savedPost, userId);
    }

    /**
     * 특정 프로젝트의 모든 게시글 조회 (최신순)
     */
    @Transactional(readOnly = true)
    @Override
    public List<BoardPostResponseDto> getPostsByProjectId(Long projectId, Long userId) {
        return boardPostRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream()
                .map(post -> BoardPostMapper.mapToResponse(post, userId)) // 람다식으로 userId 전달
                .collect(Collectors.toList());
    }

    /**
     * 게시글 부분 수정 (PATCH)
     */
    @Override
    @Transactional
    public BoardPostResponseDto patchPost(Long postId, Long userId, BoardPostRequestDto requestDto) {
        // 1. 게시글 존재 확인
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, postId));

        // 2. [보안] 작성자 권한 검증 (중요!)
        // post.getAuthor() 또는 post.getUser() 등 지호 님의 필드명에 맞춰주세요.
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        // 3. 엔티티 내부의 부분 업데이트 호출
        post.updatePost(requestDto);

        // 4. 결과 반환 (필요시 userId를 넘겨서 작성자 여부 확인)
        return BoardPostMapper.mapToResponse(post, userId);
    }

    /**
     * 게시글 삭제
     */
    @Override
    public void deletePost(Long postId, Long userId) {
        BoardPost post = boardPostRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BOARD_NOT_FOUND, postId));

        // 수정: 본인 확인 로직 추가
        if (!post.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

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
        return BoardPostMapper.mapToResponse(post, userId);
    }
}