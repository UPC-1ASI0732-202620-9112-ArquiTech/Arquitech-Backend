package com.acme.arquitech.platform.materials.interfaces.rest;
import com.acme.arquitech.platform.materials.application.internal.commandservices.MaterialCommandServiceImpl;
import com.acme.arquitech.platform.materials.application.internal.queryservices.MaterialQueryServiceImpl;
import com.acme.arquitech.platform.materials.interfaces.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/materials", produces = "application/json")
@Tag(name = "Materials")
@RequiredArgsConstructor
public class MaterialController {
    private final MaterialCommandServiceImpl commandService;
    private final MaterialQueryServiceImpl queryService;
    @GetMapping
    @Operation(summary = "List materials in your projects")
    public List<MaterialResource> all(@RequestParam(required = false) Long projectId) {
        return queryService.findAll(projectId).stream().map(MaterialResource::from).toList();
    }
    @GetMapping("/{id}")
    @Operation(summary = "Read a material")
    public MaterialResource get(@PathVariable Long id) { return MaterialResource.from(queryService.findById(id)); }
    @GetMapping("/project/{projectId}")
    @Operation(summary = "List materials in a project")
    public List<MaterialResource> byProject(@PathVariable Long projectId) { return all(projectId); }
    @PostMapping
    @Operation(summary = "Create material and initial entry")
    public ResponseEntity<MaterialResource> create(@Valid @RequestBody CreateMaterialResource r) {
        var result = MaterialResource.from(commandService.createMaterial(r.toCommand()));
        return ResponseEntity.created(java.net.URI.create("/api/v1/materials/" + result.id())).body(result);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Update descriptive material fields")
    public MaterialResource update(@PathVariable Long id, @Valid @RequestBody UpdateMaterialResource r) {
        return MaterialResource.from(commandService.updateMaterial(id, r.toCommand()));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete material and its movement history")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commandService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/entry")
    @Operation(summary = "Receive stock and record an entry")
    public ResponseEntity<MaterialMovementResource> enter(@PathVariable Long id, @Valid @RequestBody MaterialEntryResource r) {
        return ResponseEntity.status(201).body(MaterialMovementResource.from(commandService.enter(id, r.toCommand())));
    }
    @PostMapping("/{id}/use")
    @Operation(summary = "Consume stock and record usage")
    public ResponseEntity<MaterialMovementResource> use(@PathVariable Long id, @Valid @RequestBody MaterialUsageResource r) {
        return ResponseEntity.status(201).body(MaterialMovementResource.from(commandService.use(id, r.toCommand())));
    }
    @GetMapping("/project/{projectId}/history")
    @Operation(summary = "List all project material movements")
    public List<MaterialMovementResource> history(@PathVariable Long projectId) {
        return queryService.history(projectId, null).stream().map(MaterialMovementResource::from).toList();
    }
    @GetMapping("/project/{projectId}/history/{materialName}")
    @Operation(summary = "List movements by material name", deprecated = true)
    public List<MaterialMovementResource> legacyHistory(@PathVariable Long projectId, @PathVariable String materialName) {
        return queryService.history(projectId, materialName).stream().map(MaterialMovementResource::from).toList();
    }
    @GetMapping("/{id}/low-inventory")
    @Operation(summary = "Check stock threshold", deprecated = true)
    public LowInventoryResource lowInventory(@PathVariable Long id, @RequestParam(required = false) @PositiveOrZero Integer minimumLevel) {
        boolean low = queryService.isLowInventory(id, minimumLevel);
        return new LowInventoryResource(low, low ? "Material stock is below minimum level" : "Material stock is sufficient");
    }
}
