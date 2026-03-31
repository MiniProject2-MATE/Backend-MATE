package com.rookies5.Backend_MATE.mapper;

import com.rookies5.Backend_MATE.entity.User;
import org.springframework.util.StringUtils;

public class UserMapper {

    // Entity -> DTO 변환 (프론트엔드로 정보 줄 때)
    public static UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(null) // ⚠️ 보안: 프론트로 비밀번호를 절대 보내지 않음!
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .position(user.getPosition())
                .techStacks(user.getTechStacks())
                .profileImg(user.getProfileImg())
                .build();
    }

    // DTO -> Entity 변환 (회원가입 등으로 DB에 저장할 때)
    public static User mapToUser(UserDto userDto) {
        
        // 닉네임이 비어있으면 이메일 앞부분 추출 (설계서 규칙 반영)
        String finalNickname = StringUtils.hasText(userDto.getNickname()) 
                ? userDto.getNickname() 
                : userDto.getEmail().split("@")[0];

        return User.builder()
                .email(userDto.getEmail())
                .password(userDto.getPassword()) // 실제 암호화는 Service에서 진행
                .nickname(finalNickname)
                .phoneNumber(userDto.getPhoneNumber())
                .position(userDto.getPosition())
                .techStacks(userDto.getTechStacks())
                // 프로필 이미지가 안 들어오면 기본 이미지 설정
                .profileImg(userDto.getProfileImg() != null ? userDto.getProfileImg() : "https://mate-s3.com/default-profile.png")
                .build();
    }
}