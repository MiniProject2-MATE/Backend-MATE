package com.rookies5.Backend_MATE.repository;

import com.rookies5.Backend_MATE.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<조종할 Entity 클래스, 그 Entity의 PK 데이터 타입>
public interface UserRepository extends JpaRepository<User, Long> {

}