package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.ProjectMemberDto;
import java.util.List;

public interface ProjectMemberService {
    List<ProjectMemberDto> getMembersByProjectId(Long projectId);
    void removeMember(Long memberId);
}