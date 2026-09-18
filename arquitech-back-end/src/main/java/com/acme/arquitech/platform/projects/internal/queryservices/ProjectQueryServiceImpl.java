package com.acme.arquitech.platform.projects.internal.queryservices;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.services.ProjectQueryService;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectQueryServiceImpl implements ProjectQueryService {
    private final ProjectRepository projectRepository;

    public ProjectQueryServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public List<Project> findByUserIdAndRole(Long userId, Role role) {
        return projectRepository.findByUserIdAndRole(userId, role);
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }
}