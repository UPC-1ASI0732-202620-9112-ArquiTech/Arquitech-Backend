package com.acme.arquitech.platform.workers.interfaces.rest;
import com.acme.arquitech.platform.workers.application.internal.commandservices.WorkerCommandServiceImpl;
import com.acme.arquitech.platform.workers.application.internal.queryservices.WorkerQueryServiceImpl;
import com.acme.arquitech.platform.workers.interfaces.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/workers", produces = "application/json")
@Tag(name = "Workers")
@RequiredArgsConstructor
public class WorkerController {
    private final WorkerCommandServiceImpl commandService;
    private final WorkerQueryServiceImpl queryService;
    @GetMapping
    @Operation(summary = "List accessible workers, optionally by project")
    public List<WorkerResource> all(@RequestParam(required = false) Long projectId) {
        return queryService.findAll(projectId).stream().map(WorkerResource::from).toList();
    }
    @GetMapping("/{id}")
    @Operation(summary = "Read workers by ID")
    public WorkerResource get(@PathVariable Long id) { return WorkerResource.from(queryService.findById(id)); }
    @PostMapping
    @Operation(summary = "Create workers in a supervised project")
    public ResponseEntity<WorkerResource> create(@Valid @RequestBody CreateWorkerResource resource) {
        var result = WorkerResource.from(commandService.create(resource.toCommand()));
        return ResponseEntity.created(java.net.URI.create("/api/v1/workers/" + result.id())).body(result);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update workers")
    public WorkerResource update(@PathVariable Long id, @Valid @RequestBody UpdateWorkerResource resource) {
        return WorkerResource.from(commandService.update(id, resource.toCommand()));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete workers")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
