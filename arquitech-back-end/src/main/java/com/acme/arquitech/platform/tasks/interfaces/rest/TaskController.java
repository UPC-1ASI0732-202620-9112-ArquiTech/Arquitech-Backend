package com.acme.arquitech.platform.tasks.interfaces.rest;
import com.acme.arquitech.platform.tasks.application.internal.commandservices.TaskCommandServiceImpl;
import com.acme.arquitech.platform.tasks.application.internal.queryservices.TaskQueryServiceImpl;
import com.acme.arquitech.platform.tasks.interfaces.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/tasks", produces = "application/json")
@Tag(name = "Tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskCommandServiceImpl commandService;
    private final TaskQueryServiceImpl queryService;
    @GetMapping
    @Operation(summary = "List accessible tasks, optionally by project")
    public List<TaskResource> all(@RequestParam(required = false) Long projectId) {
        return queryService.findAll(projectId).stream().map(TaskResource::from).toList();
    }
    @PostMapping
    @Operation(summary = "Create tasks in a supervised project")
    public ResponseEntity<TaskResource> create(@Valid @RequestBody CreateTaskResource resource) {
        var result = TaskResource.from(commandService.create(resource.toCommand()));
        return ResponseEntity.created(java.net.URI.create("/api/v1/tasks/" + result.id())).body(result);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update tasks")
    public TaskResource update(@PathVariable Long id, @Valid @RequestBody UpdateTaskResource resource) {
        return TaskResource.from(commandService.update(id, resource.toCommand()));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tasks")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
