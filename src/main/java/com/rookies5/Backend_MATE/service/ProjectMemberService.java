package com.rookies5.Backend_MATE.service;

import java.util.List;

public interface ProjectMemberService {
    List<ProjectMemberDto> getMembersByProjectId(Long projectId);
    void removeMember(Long memberId);
}