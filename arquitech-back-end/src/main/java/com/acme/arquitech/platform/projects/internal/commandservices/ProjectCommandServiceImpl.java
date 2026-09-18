package com.acme.arquitech.platform.projects.internal.commandservices;


import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import com.acme.arquitech.platform.projects.domain.services.ProjectCommandService;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ProjectCommandServiceImpl implements ProjectCommandService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectCommandServiceImpl(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Project create(String name, LocalDate startDate, LocalDate endDate, BigDecimal budget, ProjectStatus status, Long userId, Long contractorId, String imageUrl) {
        // Validate user and contractor exist
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found"));
        User contractor = userRepository.findById(contractorId)
                .orElseThrow(() -> new IllegalArgumentException("Contractor with ID " + contractorId + " not found"));

        // Create project
        Project project = new Project(name, startDate, endDate, budget, status, user, contractor, imageUrl);
        return projectRepository.save(project);
    }
}