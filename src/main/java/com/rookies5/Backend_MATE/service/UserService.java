package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.request.UserRequestDto;
import com.rookies5.Backend_MATE.dto.response.UserResponseDto;

import java.util.List;

public interface UserService {

    /**
     * 새로운 사용자(회원) 생성
     * @param requestDto 회원가입 정보 (이메일, 비밀번호, 닉네임 등)
     * @return 생성된 사용자 정보 (비밀번호 제외)
     */
    UserResponseDto createUser(UserRequestDto requestDto);

    /**
     * 특정 사용자 상세 정보 조회
     * @param userId 조회할 사용자 ID
     * @return 사용자 프로필 데이터
     */
    UserResponseDto getUserById(Long userId);

    /**
     * 전체 사용자 목록 조회 (관리자용 또는 매칭 추천용)
     * @return 전체 사용자 리스트
     */
    List<UserResponseDto> getAllUsers();

    /**
     * 사용자 프로필 정보 수정
     * @param userId 수정할 사용자 ID
     * @param requestDto 수정할 내용 (닉네임, 포지션, 기술 스택 등)
     * @return 수정 완료된 사용자 정보
     */
    UserResponseDto updateUser(Long userId, UserRequestDto requestDto);

    /**
     * 회원 탈퇴 (사용자 삭제)
     * @param userId 삭제할 사용자 ID
     */
    void deleteUser(Long userId);

    /**
     * 닉네임 중복 체크
     * @param nickname 검사할 닉네임
     * @return 중복 여부 (true: 중복됨, false: 사용 가능)
     */
    boolean checkNicknameDuplicate(String nickname);

    /**
     * 7. 아이디(이메일) 찾기
     */
    String findEmailByPhoneNumber(String phoneNumber);

    /**
     * 8. 비밀번호 재설정 (임시 비밀번호 반환)
     */
    String resetPassword(String email, String phoneNumber);
}