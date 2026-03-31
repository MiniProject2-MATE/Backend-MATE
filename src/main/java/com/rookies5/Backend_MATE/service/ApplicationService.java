package com.rookies5.Backend_MATE.service;

import com.rookies5.Backend_MATE.dto.ApplicationDto;
import java.util.List;

public interface ApplicationService {
    ApplicationDto applyToProject(ApplicationDto applicationDto);
    List<ApplicationDto> getApplicationsByProjectId(Long projectId);
    void deleteApplication(Long applicationId);
}