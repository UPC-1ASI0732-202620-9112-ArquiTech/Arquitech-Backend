package com.acme.arquitech.platform.machinery.interfaces.rest;
import com.acme.arquitech.platform.machinery.application.internal.commandservices.MachineryCommandServiceImpl;
import com.acme.arquitech.platform.machinery.application.internal.queryservices.MachineryQueryServiceImpl;
import com.acme.arquitech.platform.machinery.interfaces.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/machinery", produces = "application/json")
@Tag(name = "Machinery")
@RequiredArgsConstructor
public class MachineryController {
    private final MachineryCommandServiceImpl commandService;
    private final MachineryQueryServiceImpl queryService;
    @GetMapping
    @Operation(summary = "List accessible machinery, optionally by project")
    public List<MachineryResource> all(@RequestParam(required = false) Long projectId) {
        return queryService.findAll(projectId).stream().map(MachineryResource::from).toList();
    }
    @GetMapping("/{id}")
    @Operation(summary = "Read machinery by ID")
    public MachineryResource get(@PathVariable Long id) { return MachineryResource.from(queryService.findById(id)); }
    @PostMapping
    @Operation(summary = "Create machinery in a supervised project")
    public ResponseEntity<MachineryResource> create(@Valid @RequestBody CreateMachineryResource resource) {
        var result = MachineryResource.from(commandService.create(resource.toCommand()));
        return ResponseEntity.created(java.net.URI.create("/api/v1/machinery/" + result.id())).body(result);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update machinery")
    public MachineryResource update(@PathVariable Long id, @Valid @RequestBody UpdateMachineryResource resource) {
        return MachineryResource.from(commandService.update(id, resource.toCommand()));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete machinery")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
