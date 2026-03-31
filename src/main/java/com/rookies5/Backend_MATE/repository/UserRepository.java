package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<조종할 Entity 클래스, 그 Entity의 PK 데이터 타입>
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    // 4.1.6 전화번호 중복 확인
    boolean existsByPhoneNumber(String phoneNumber);

    // 4.1.4 아이디 찾기용
    Optional<User> findByPhoneNumber(String phoneNumber);

    // 4.1.7 닉네임 중복 확인
    boolean existsByNickname(String nickname);

    // 4.1.5 비밀번호 찾기용 (이메일과 전화번호 동시 만족)
    Optional<User> findByEmailAndPhoneNumber(String email, String phoneNumber);

}