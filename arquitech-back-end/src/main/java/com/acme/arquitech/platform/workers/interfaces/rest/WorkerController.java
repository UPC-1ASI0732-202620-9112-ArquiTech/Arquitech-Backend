package com.acme.arquitech.platform.workers.interfaces.rest;

import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.MessageResource;
import com.acme.arquitech.platform.workers.application.internal.commandservices.WorkerCommandServiceImpl;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerName;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerRole;
import com.acme.arquitech.platform.workers.interfaces.rest.resources.CreateWorkerResource;
import com.acme.arquitech.platform.workers.interfaces.rest.resources.UpdateWorkerResource;
import com.acme.arquitech.platform.workers.interfaces.rest.resources.WorkerResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/workers")
@Tag(name = "Workers", description = "Worker Management Endpoints")
public class WorkerController {
    private final WorkerCommandServiceImpl workerCommandService;
    private final ProjectRepository projectRepository;

    public WorkerController(WorkerCommandServiceImpl workerCommandService, ProjectRepository projectRepository) {
        this.workerCommandService = workerCommandService;
        this.projectRepository = projectRepository;
    }

    @PostMapping
    public ResponseEntity<WorkerResource> createWorker(@RequestBody CreateWorkerResource resource) {
        Project project = new Project();
        project.setId(resource.projectId()); // Inherit projectId without needing @PathVariable
        Worker worker = new Worker(
                new WorkerName(resource.name()),
                new WorkerRole(resource.role()),
                resource.hiredDate(),
                project
        );

        Worker savedWorker = workerCommandService.create(worker);

        WorkerResource response = new WorkerResource(
                savedWorker.getId(),
                savedWorker.getName().value(),
                savedWorker.getRole().value(),
                savedWorker.getHiredDate(),
                savedWorker.getProject().getId()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{workerId}")
    public ResponseEntity<WorkerResource> updateWorker(@PathVariable Long projectId, @PathVariable Long workerId, @Valid @RequestBody UpdateWorkerResource resource) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with ID " + projectId + " not found"));
        Worker existingWorker = workerCommandService.findById(workerId)
                .orElseThrow(() -> new com.acme.arquitech.platform.workers.domain.exceptions.WorkerNotFoundException(workerId));

        existingWorker.setName(new WorkerName(resource.name()));
        existingWorker.setRole(new WorkerRole(resource.role()));
        existingWorker.setHiredDate(resource.hiredDate());
        existingWorker.setProject(project);

        Worker updatedWorker = workerCommandService.update(existingWorker);
        WorkerResource response = new WorkerResource(
                updatedWorker.getId(),
                updatedWorker.getName().value(),
                updatedWorker.getRole().value(),
                updatedWorker.getHiredDate(),
                updatedWorker.getProject().getId()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workerId}")
    public ResponseEntity<MessageResource> deleteWorker(@PathVariable Long projectId, @PathVariable Long workerId) {
        workerCommandService.delete(workerId);
        return ResponseEntity.ok(new MessageResource("Worker deleted successfully"));
    }



    @GetMapping
    public ResponseEntity<List<WorkerResource>> getAllWorkers() {
        List<Worker> workers = workerCommandService.getAll();
        List<WorkerResource> resources = workers.stream()
                .map(worker -> new WorkerResource(
                        worker.getId(),
                        worker.getName().value(),
                        worker.getRole().value(),
                        worker.getHiredDate(),
                        worker.getProject().getId()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }
}