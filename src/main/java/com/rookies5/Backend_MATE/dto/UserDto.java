package com.rookies5.Backend_MATE.dto;

import com.rookies5.Backend_MATE.entity.enums.Position;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;

    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;

    @NotBlank(message = "닉네임은 필수입니다.")
    private String nickname;

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    private Position position; // FE, BE, DE 등

    private Set<String> techStacks; // ["Java", "Spring Boot"]

    private String profileImg;
}