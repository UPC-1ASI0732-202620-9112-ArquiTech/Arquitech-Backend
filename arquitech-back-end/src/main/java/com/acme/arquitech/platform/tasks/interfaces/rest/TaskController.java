package com.acme.arquitech.platform.tasks.interfaces.rest;

import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.MessageResource;
import com.acme.arquitech.platform.tasks.application.internal.commandservices.TaskCommandServiceImpl;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import com.acme.arquitech.platform.tasks.interfaces.rest.resources.CreateTaskResource;
import com.acme.arquitech.platform.tasks.interfaces.rest.resources.TaskResource;
import com.acme.arquitech.platform.tasks.interfaces.rest.resources.UpdateTaskResource;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tasks")
@Tag(name = "Tasks", description = "Task Management Endpoints")
public class TaskController {
    private final TaskCommandServiceImpl taskCommandService;
    private final ProjectRepository projectRepository;
    private final WorkerRepository workerRepository;

    public TaskController(TaskCommandServiceImpl taskCommandService, ProjectRepository projectRepository, WorkerRepository workerRepository) {
        this.taskCommandService = taskCommandService;
        this.projectRepository = projectRepository;
        this.workerRepository = workerRepository;
    }
    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        List<Task> tasks = taskCommandService.findAll();
        if (tasks.isEmpty()) {
            return ResponseEntity.ok(new MessageResource("No tasks registered"));
        }
        List<TaskResource> resources = tasks.stream()
                .map(task -> new TaskResource(
                        task.getId(),
                        task.getProject().getId(),
                        task.getWorker().getId(),
                        task.getDescription(),
                        task.getStartDate(),
                        task.getDueDate(),
                        task.getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }


    @PostMapping
    public ResponseEntity<TaskResource> createTask(@RequestBody CreateTaskResource resource) {
        try {
            Project project = new Project();
            project.setId(resource.projectId());
            Worker worker = new Worker();
            worker.setId(resource.workerId());

            Task task = new Task(
                    project,
                    worker,
                    resource.description(),
                    resource.startDate(),
                    resource.dueDate(),
                    resource.status()
            );

            Task savedTask = taskCommandService.create(task);

            TaskResource response = new TaskResource(
                    savedTask.getId(),
                    savedTask.getProject().getId(),
                    savedTask.getWorker().getId(),
                    savedTask.getDescription(),
                    savedTask.getStartDate(),
                    savedTask.getDueDate(),
                    savedTask.getStatus()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResource> updateTask(@PathVariable Long projectId, @PathVariable Long workerId, @PathVariable Long taskId, @Valid @RequestBody UpdateTaskResource resource) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project with ID " + projectId + " not found"));
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker with ID " + workerId + " not found"));
        Task existingTask = taskCommandService.findById(taskId)
                .orElseThrow(() -> new com.acme.arquitech.platform.tasks.domain.exceptions.TaskNotFoundException(taskId));

        existingTask.setDescription(resource.description());
        existingTask.setStartDate(resource.startDate());
        existingTask.setDueDate(resource.dueDate());
        existingTask.setStatus(resource.status());
        existingTask.setProject(project);
        existingTask.setWorker(worker);

        Task updatedTask = taskCommandService.update(existingTask);
        TaskResource response = new TaskResource(
                updatedTask.getId(),
                updatedTask.getProject().getId(),
                updatedTask.getWorker().getId(),
                updatedTask.getDescription(),
                updatedTask.getStartDate(),
                updatedTask.getDueDate(),
                updatedTask.getStatus()
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<MessageResource> deleteTask(@PathVariable Long projectId, @PathVariable Long workerId, @PathVariable Long taskId) {
        taskCommandService.delete(taskId);
        return ResponseEntity.ok(new MessageResource("Task deleted successfully"));
    }
}