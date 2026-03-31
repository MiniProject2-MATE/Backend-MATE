package com.rookies5.Backend_MATE.dto;

import com.rookies5.Backend_MATE.entity.enums.MemberRole;
import com.rookies5.Backend_MATE.entity.enums.Position;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMemberDto {
    private Long id;
    private Long projectId;
    private Long userId;
    
    private MemberRole role; // 방장(OWNER)인지 팀원(MEMBER)인지 구분

    // 💡 프론트엔드에서 팀원 목록을 그릴 때 필요한 추가 데이터
    private String nickname;
    private String profileImg;
    private Position position;
}