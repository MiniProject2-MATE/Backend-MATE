package com.rookies5.Backend_MATE.service.impl;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.ApplicationResponseDto;
import com.rookies5.Backend_MATE.dto.response.ProjectResponseDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;
import com.rookies5.Backend_MATE.entity.User;
import com.rookies5.Backend_MATE.exception.BusinessException;
import com.rookies5.Backend_MATE.exception.EntityNotFoundException;
import com.rookies5.Backend_MATE.exception.ErrorCode;
import com.rookies5.Backend_MATE.mapper.ApplicationMapper;
import com.rookies5.Backend_MATE.mapper.ProjectMapper;
import com.rookies5.Backend_MATE.mapper.UserMapper;
import com.rookies5.Backend_MATE.repository.ApplicationRepository;
import com.rookies5.Backend_MATE.repository.ProjectRepository;
import com.rookies5.Backend_MATE.repository.UserRepository;
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
     * 3. 내 정보 수정 (닉네임 중복 검사 포함)
     */
    @Override
    public UserResponseDto updateUser(Long userId, UserRequestDto requestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 닉네임 변경 시에만 중복 체크 실행
        if (requestDto.getNickname() != null && !user.getNickname().equals(requestDto.getNickname())) {
            // 이 메서드 안에서 중복/형식 에러를 다 던져주므로 호출만 하면 끝!
            isNicknameAvailable(requestDto.getNickname(), userId);
        }

        // 전화번호 변경 시 중복 체크
        if (requestDto.getPhoneNumber() != null && !user.getPhoneNumber().equals(requestDto.getPhoneNumber())) {
            if (userRepository.existsByPhoneNumber(requestDto.getPhoneNumber())) {
                throw new BusinessException(ErrorCode.USER_PHONE_DUPLICATE);
            }
        }

        user.updateProfile(
                requestDto.getNickname(),
                requestDto.getPosition(),
                requestDto.getTechStacks(),
                requestDto.getProfileImg(),
                requestDto.getPhoneNumber()
        );

        return UserMapper.mapToUserResponse(user);
    }

    /**
     * 4. 회원 탈퇴 (Hard Delete -> Soft Delete 전환 준비)
     */
    @Override
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        // 지호 님, 아직 softDelete 구현 전이라 하셨으니 일단 Hard Delete 유지합니다.
        // 나중에 엔티티에 필드 추가 후 user.softDelete()로 바꾸시면 됩니다!
        userRepository.delete(user);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND, userId));

        if (profileImage == null || profileImage.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR);
        }

        try {
            // 1. 기존 파일 삭제 로직 (추가된 부분)
            String oldImgUrl = user.getProfileImg();
            // 기본 이미지(Pixabay 등)가 아니고, 우리가 저장한 로컬 경로일 때만 삭제
            if (oldImgUrl != null && oldImgUrl.startsWith("/uploads/profiles/")) {
                // DB 경로 "/uploads/profiles/uuid.jpg"에서 파일명만 추출
                String oldFileName = oldImgUrl.replace("/uploads/profiles/", "");
                java.io.File oldFile = new java.io.File(uploadPath + oldFileName);

                if (oldFile.exists()) {
                    oldFile.delete(); // 실제 파일 삭제
                    log.info("기존 프로필 이미지 삭제 완료: {}", oldFileName);
                }
            }

            // 2. 폴더 확인 및 생성
            java.io.File folder = new java.io.File(uploadPath);
            if (!folder.exists()) folder.mkdirs();

            // 3. 새 파일명 생성 (UUID)
            String originalFilename = profileImage.getOriginalFilename();
            String extension = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
            String storeFilename = java.util.UUID.randomUUID().toString() + extension;

            // 4. 새 파일 저장
            profileImage.transferTo(new java.io.File(uploadPath + storeFilename));

            // 5. DB 업데이트
            String newImgUrl = "/uploads/profiles/" + storeFilename;
            user.updateProfileImg(newImgUrl);

            return newImgUrl;

        } catch (java.io.IOException e) {
            log.error("파일 처리 중 오류 발생: {}", e.getMessage());
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

        // 1. 기존에 저장된 '실제 파일'이 있다면 삭제 시도
        String currentImgUrl = user.getProfileImg();

        // 우리가 저장한 로컬 경로(/uploads/profiles/)인 경우에만 파일을 지웁니다.
        if (currentImgUrl != null && currentImgUrl.startsWith("/uploads/profiles/")) {
            try {
                // 경로에서 파일명만 추출 (예: uuid.jpg)
                String fileName = currentImgUrl.replace("/uploads/profiles/", "");
                java.io.File fileToDelete = new java.io.File(uploadPath + fileName);

                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        log.info("기존 프로필 이미지 파일 삭제 성공: {}", fileName);
                    } else {
                        log.warn("파일 삭제 실패 (권한 문제 등): {}", fileName);
                    }
                }
            } catch (Exception e) {
                log.error("파일 삭제 중 예상치 못한 오류 발생: {}", e.getMessage());
                // 파일 삭제 실패가 비즈니스 로직을 멈출 정도는 아니므로 예외를 던지지는 않습니다.
            }
        }

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
        // ProjectService에 위임하여 내가 방장인 글 목록을 가져옵니다.
        return projectService.getMyOwnedPosts(userId);
    }

    /**
     * 10. 참여 중인 프로젝트/스터디 조회 (승인 완료 - 명세서 4.5.3-2)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponseDto> getMyJoinedProjects(Long userId) {
        // ProjectService에 위임하여 내가 멤버로 참여 중인 글 목록을 가져옵니다.
        return projectService.getMyJoinedProjects(userId);
    }

    /**
     * 11. 내 지원 현황 조회 (대기/거절 상태 - 명세서 4.5.3-3)
     */
    @Transactional(readOnly = true)
    @Override
    public List<ApplicationResponseDto> getMyPendingApplications(Long userId) {
        // ApplicationService에 위임하여 승인 대기/거절 상태인 내역을 가져옵니다.
        return applicationService.getMyPendingApplications(userId);
    }
}