package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.Project;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.ApplicationMapper;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.*;
// import com.rookies5.Backend_MATE.repository.ProjectRepository;     // 나중에 추가 시 주석 해제
// import com.rookies5.Backend_MATE.repository.ApplicationRepository; // 나중에 추가 시 주석 해제
import com.rookies5.Backend_MATE.service.ApplicationService;
import com.rookies5.Backend_MATE.service.ProjectService;
import com.rookies5.Backend_MATE.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final ApplicationService applicationService;
    private final ProjectRepository projectRepository;
    private final BoardPostRepository boardPostRepository;
    private final CommentRepository commentRepository;
    private final ApplicationRepository applicationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final String uploadPath = System.getProperty("user.home") + "/mate_uploads/profiles/";

    private static final String DEFAULT_PROFILE_IMG = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";

    /**
     * 1. 내 정보 상세 조회
     */
    @Transactional(readOnly = true)
    @Override
    public UserResponseDto getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(UserMapper::mapToUserResponse)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));
    }

    /**
     * 2. 전체 회원 조회 (관리자용)
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::mapToUserResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. 회원 정보 수정
     */
    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 1. 닉네임 변경 시 중복 체크
        if (requestDto.getNickname() != null && !user.getNickname().equals(requestDto.getNickname())) {
            isNicknameAvailable(requestDto.getNickname(), userId);
        }

        // 2. 전화번호 변경 시 중복 체크
        if (requestDto.getPhoneNumber() != null && !user.getPhoneNumber().equals(requestDto.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
            }
        }

        // 3. 엔티티 메서드 호출
        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getPosition(),
                requestDto.getTechStacks(),
                requestDto.getPhoneNumber()
        );

        return UserMapper.mapToUserResponse(user);
    }

    /**
     * 4. 회원 탈퇴
     */
    @Override
    public void deleteUser(Long userId, User currentUser) {
        // 본인 확인 로직 추가
        // 탈퇴하려는 대상(userId)이 현재 로그인한 사람(currentUser)과 다르면 예외 발생
        if (!userId.equals(currentUser.getId())) {
            throw new BusinessException(ErrorCode.AUTH_ACCESS_DENIED);
        }

        // 유저 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 이 유저가 방장인 프로젝트들 폭파
        List<Project> ownedProjects = projectRepository.findAllByOwnerId(userId);
        for (Project project : ownedProjects) {
            projectService.deleteProject(project.getId(), userId);
        }

        // 이 유저가 작성한 게시글, 댓글 등 흔적 지우기
        boardPostRepository.softDeleteAllByAuthorId(userId);
        commentRepository.softDeleteAllByAuthorId(userId);
        applicationRepository.softDeleteAllByApplicantId(userId);
        projectMemberRepository.softDeleteAllByUserId(userId);

        //refresh 토큰은 DB에서 완전 삭제 (Hard-delete)
        refreshTokenRepository.deleteByUserId(userId);

        // 유저 테이블의 deleted_at 업데이트 쿼리 실행!
        userRepository.softDeleteById(userId);

        log.info("회원 탈퇴 완료 - 유저 ID: {}", userId);
    }

    /**
     * 5. 닉네임 중복 및 유효성 확인
     */
    @Override
    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname, Long currentUserId) {
        // 1. 유효성 검사 (형식 에러 - USER_007)
        String regex = "^[a-zA-Z0-9가-힣]{2,10}$";
        if (nickname == null || !nickname.matches(regex)) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_FORMAT_INVALID);
        }

        // 2. 중복 체크 (진짜 중복 에러 - USER_003)
        boolean isDuplicate;
        if (currentUserId == null) {
            // 회원가입 시: 전체 중복 체크
            isDuplicate = userRepository.existsByNicknameIgnoreCase(nickname);
        } else {
            // 마이페이지 수정 시: '나'를 제외하고 중복 체크 (UserIdNot -> IdNot으로 수정)
            isDuplicate = userRepository.existsByNicknameIgnoreCaseAndIdNot(nickname, currentUserId);
        }

        if (isDuplicate) {
            throw new BusinessException(ErrorCode.USER_NICKNAME_DUPLICATE);
        }

        return true;
    }

    /**
     * 6. 전화번호 중복 확인
     */
    @Transactional(readOnly = true)
    @Override
    public boolean checkPhoneDuplicate(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 7. 프로필 이미지 수정 (로컬 저장 방식 적용)
     */
    @Override
    public String updateProfileImage(Long userId, MultipartFile profileImage) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 2. 파일 유효성 검사
        if (profileImage == null || profileImage.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "업로드할 이미지가 없습니다.");
        }

        try {
            // 3. 공통 메서드를 사용하여 기존 파일 삭제
            deleteActualFile(user.getProfileImg());

            // 4. 저장 폴더 생성
            java.io.File folder = new java.io.File(uploadPath);
            if (!folder.exists()) folder.mkdirs();

            // 5. 새 파일명 생성 (UUID 활용)
            String originalFilename = profileImage.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String storeFilename = java.util.UUID.randomUUID().toString() + extension;

            // 6. 새 파일 실제 저장
            profileImage.transferTo(new java.io.File(uploadPath + storeFilename));

            // 7. DB 경로 업데이트 및 반환
            String newImgUrl = "/uploads/profiles/" + storeFilename;
            user.updateProfileImg(newImgUrl);

            log.info("유저(ID: {})의 프로필 이미지가 업데이트되었습니다: {}", userId, storeFilename);
            return newImgUrl;

        } catch (java.io.IOException e) {
            log.error("파일 저장 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 8. 프로필 이미지 삭제 (서버 파일 삭제 + 기본 이미지로 복구)
     */
    @Override
    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 1. 공통 메서드를 사용하여 실제 파일 삭제
        deleteActualFile(user.getProfileImg());

        // 2. DB 정보는 기본 이미지 URL로 변경
        user.updateProfileImg(DEFAULT_PROFILE_IMG);

        log.info("유저(ID: {})의 프로필 이미지가 기본 이미지로 초기화되었습니다.", userId);
    }

    /**
     * 9. 내가 작성한 모집글 조회 (내가 방장인 것 - 명세서 4.5.3-1)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponseDto> getMyOwnedPosts(Long userId) {
        return projectService.getMyOwnedPosts(userId);
    }

    /**
     * 10. 참여 중인 프로젝트/스터디 조회 (승인 완료 - 명세서 4.5.3-2)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponseDto> getMyJoinedProjects(Long userId) {
        return projectService.getMyJoinedProjects(userId);
    }

    /**
     * 11. 내 지원 현황 조회 (대기/거절 상태 - 명세서 4.5.3-3)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ApplicationResponseDto> getMyPendingApplications(Long userId) {
        return applicationService.getMyPendingApplications(userId);
    }

    /**
     * [공통] 로컬 서버에 저장된 실제 파일을 삭제하는 내부 메서드
     */
    private void deleteActualFile(String imgUrl) {
        // 우리가 관리하는 경로(/uploads/profiles/)인 경우에만 파일 삭제 시도
        if (imgUrl != null && imgUrl.startsWith("/uploads/profiles/")) {
            try {
                String fileName = imgUrl.replace("/uploads/profiles/", "");
                java.io.File fileToDelete = new java.io.File(uploadPath + fileName);

                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        log.info("로컬 파일 삭제 성공: {}", fileName);
                    } else {
                        log.warn("로컬 파일 삭제 실패 (권한 등): {}", fileName);
                    }
                }
            } catch (Exception e) {
                log.error("파일 삭제 중 오류 발생: {}", e.getMessage());
            }
        }
    }
}