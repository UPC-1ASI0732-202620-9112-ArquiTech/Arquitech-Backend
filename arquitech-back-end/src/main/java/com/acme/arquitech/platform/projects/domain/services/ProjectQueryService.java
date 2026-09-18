package com.acme.arquitech.platform.projects.domain.services;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;

import java.util.List;

public interface ProjectQueryService {
    List<Project> findByUserIdAndRole(Long userId, Role role);
}