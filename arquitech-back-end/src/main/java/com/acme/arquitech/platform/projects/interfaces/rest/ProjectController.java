package com.acme.arquitech.platform.projects.interfaces.rest;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.projects.domain.services.*;
import com.acme.arquitech.platform.projects.interfaces.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/projects", produces = "application/json")
@Tag(name = "Projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectQueryService queryService;
    private final ProjectCommandService commandService;
    @GetMapping
    @Operation(summary = "List your accessible projects")
    public List<ProjectResource> getAll() { return queryService.findAll().stream().map(ProjectResource::from).toList(); }
    @GetMapping("/supervisor/{userId}")
    @Operation(summary = "List your supervised projects")
    public List<ProjectResource> supervisor(@PathVariable Long userId) {
        return queryService.findByUserIdAndRole(userId, Role.SUPERVISOR).stream().map(ProjectResource::from).toList();
    }
    @PostMapping
    @Operation(summary = "Create a project as its supervisor")
    public ResponseEntity<ProjectResource> create(@Valid @RequestBody CreateProjectResource resource) {
        var result = ProjectResource.from(commandService.create(resource.toCommand()));
        return ResponseEntity.created(java.net.URI.create("/api/v1/projects/" + result.id())).body(result);
    }
}
