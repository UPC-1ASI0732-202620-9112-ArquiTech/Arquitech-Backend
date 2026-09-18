package com.acme.arquitech.platform.projects.interfaces.rest;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.projects.internal.commandservices.ProjectCommandServiceImpl;
import com.acme.arquitech.platform.projects.internal.queryservices.ProjectQueryServiceImpl;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.interfaces.rest.resources.CreateProjectResource;
import com.acme.arquitech.platform.projects.interfaces.rest.resources.ProjectResource;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value="/api/v1/projects", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Projects", description = "Project Management Endpoints")
public class ProjectController {
    private final ProjectQueryServiceImpl projectQueryService;
    private final ProjectCommandServiceImpl projectCommandService;

    public ProjectController(ProjectQueryServiceImpl projectQueryService, ProjectCommandServiceImpl projectCommandService) {
        this.projectQueryService = projectQueryService;
        this.projectCommandService = projectCommandService;
    }


        @GetMapping
        public ResponseEntity<List<ProjectResource>> getAllProjects() {
            List<Project> projects = projectQueryService.findAll();
            if (projects.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            List<ProjectResource> resources = projects.stream()
                    .map(project -> new ProjectResource(
                            project.getId(),
                            project.getName(),
                            project.getStartDate(),
                            project.getEndDate(),
                            project.getBudget(),
                            project.getStatus(),
                            project.getUser().getId(),
                            project.getContractor().getId(),
                            project.getImageUrl()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(resources);
        }


    @GetMapping("/supervisor/{userId}")
    public ResponseEntity<?> getProjectsBySupervisor(@PathVariable Long userId) {
        List<Project> projects = projectQueryService.findByUserIdAndRole(userId, Role.SUPERVISOR);
        if (projects.isEmpty()) {
            return ResponseEntity.ok(new MessageResource("No projects registered for this supervisor"));
        }
        List<ProjectResource> resources = projects.stream()
                .map(project -> new ProjectResource(
                        project.getId(),
                        project.getName(),
                        project.getStartDate(),
                        project.getEndDate(),
                        project.getBudget(),
                        project.getStatus(),
                        project.getUser().getId(),
                        project.getContractor().getId(),
                        project.getImageUrl()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PostMapping
    public ResponseEntity<ProjectResource> createProject(@Valid @RequestBody CreateProjectResource createProjectResource) {
        Project savedProject = projectCommandService.create(
                createProjectResource.name(),
                createProjectResource.startDate(),
                createProjectResource.endDate(),
                createProjectResource.budget(),
                createProjectResource.status(),
                createProjectResource.userId(),
                createProjectResource.contractorId(),
                createProjectResource.imageUrl()
        );
        ProjectResource resource = new ProjectResource(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getStartDate(),
                savedProject.getEndDate(),
                savedProject.getBudget(),
                savedProject.getStatus(),
                savedProject.getUser().getId(),
                savedProject.getContractor().getId(),
                savedProject.getImageUrl()
        );
        return ResponseEntity.status(201).body(resource);
    }
}