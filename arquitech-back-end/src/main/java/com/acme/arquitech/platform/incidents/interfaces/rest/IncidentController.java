package com.acme.arquitech.platform.incidents.interfaces.rest;
import com.acme.arquitech.platform.incidents.internal.commandservices.IncidentCommandServiceImpl;
import com.acme.arquitech.platform.incidents.internal.queryservices.IncidentQueryServiceImpl;
import com.acme.arquitech.platform.incidents.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/incidents", produces = "application/json")
@Tag(name = "Incidents")
@RequiredArgsConstructor
public class IncidentController {
    private final IncidentCommandServiceImpl commandService;
    private final IncidentQueryServiceImpl queryService;
    @GetMapping("/project/{projectId}")
    @Operation(summary = "List incidents in a project")
    public List<IncidentResource> project(@PathVariable Long projectId) {
        return queryService.findAll(projectId).stream().map(IncidentResource::from).toList();
    }
    @PostMapping
    @Operation(summary = "Report an incident as the authenticated supervisor")
    public ResponseEntity<IncidentResource> create(@Valid @RequestBody CreateIncidentResource r) {
        var result = IncidentResource.from(commandService.create(r.toCommand()));
        return ResponseEntity.created(java.net.URI.create("/api/v1/incidents/" + result.id())).body(result);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update an incident and its resolution status")
    public IncidentResource update(@PathVariable Long id, @Valid @RequestBody UpdateIncidentResource r) {
        return IncidentResource.from(commandService.update(id, r.toCommand()));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
