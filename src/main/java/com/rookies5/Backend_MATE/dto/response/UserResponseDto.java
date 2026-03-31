package com.rookies5.Backend_MATE.dto.response;

import com.rookies5.Backend_MATE.entity.enums.Position;
import lombok.Builder;
import lombok.Getter;
import java.util.Set;

@Getter
@Builder
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String phoneNumber;
    private Position position;
    private Set<String> techStacks;
    private String profileImg;
}