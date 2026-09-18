package com.acme.arquitech.platform.machinery.interfaces.rest;

import com.acme.arquitech.platform.machinery.application.internal.commandservices.MachineryCommandServiceImpl;
import com.acme.arquitech.platform.machinery.domain.exception.MachineryNotFoundException;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import com.acme.arquitech.platform.machinery.interfaces.rest.resources.CreateMachineryResource;
import com.acme.arquitech.platform.machinery.interfaces.rest.resources.MachineryResource;
import com.acme.arquitech.platform.machinery.interfaces.rest.resources.UpdateMachineryResource;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/machinery")
@Tag(name = "Machinery", description = "API for managing machinery in construction projects")
public class MachineryController {

    private final MachineryCommandServiceImpl machineryService;

    public MachineryController(MachineryCommandServiceImpl machineryService) {
        this.machineryService = machineryService;
    }

    @Operation(summary = "Create a new machinery entry")
    @PostMapping
    public ResponseEntity<?> createMachinery(@RequestBody CreateMachineryResource resource) {
        try {
            var machinery = machineryService.create(new Machinery(
                    resource.projectId(),
                    resource.name(),
                    resource.licensePlate(),
                    resource.registerDate(),
                    resource.status()
            ));
            var machineryResource = new MachineryResource(
                    machinery.getId(),
                    machinery.getProjectId(),
                    machinery.getName(),
                    machinery.getLicensePlate(),
                    machinery.getRegisterDate(),
                    machinery.getStatus()
            );
            return new ResponseEntity<>(machineryResource, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new MessageResource(e.getMessage()), HttpStatus.CONFLICT);
        }
    }

    @Operation(summary = "Get all machinery")
    @GetMapping
    public ResponseEntity<List<MachineryResource>> getAllMachinery() {
        var machineries = machineryService.getAll();
        var machineryResources = machineries.stream()
                .map(m -> new MachineryResource(
                        m.getId(),
                        m.getProjectId(),
                        m.getName(),
                        m.getLicensePlate(),
                        m.getRegisterDate(),
                        m.getStatus()
                ))
                .collect(Collectors.toList());
        return new ResponseEntity<>(machineryResources, HttpStatus.OK);
    }

    @Operation(summary = "Get machinery by ID")
    @GetMapping("/{id}")
    public ResponseEntity<MachineryResource> getMachineryById(@PathVariable Long id) {
        var machinery = machineryService.getById(id)
                .orElseThrow(() -> new MachineryNotFoundException(id));
        var machineryResource = new MachineryResource(
                machinery.getId(),
                machinery.getProjectId(),
                machinery.getName(),
                machinery.getLicensePlate(),
                machinery.getRegisterDate(),
                machinery.getStatus()
        );
        return new ResponseEntity<>(machineryResource, HttpStatus.OK);
    }
    @Operation(summary = "Update an existing machinery entry")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMachinery(@PathVariable Long id, @RequestBody UpdateMachineryResource resource) {
        try {
            var existingMachinery = machineryService.getById(id)
                    .orElseThrow(() -> new MachineryNotFoundException(id));
            var updatedMachinery = new Machinery(
                    existingMachinery.getProjectId(),
                    resource.name(),
                    resource.licensePlate(),
                    resource.registerDate(),
                    resource.status()
            );

            var savedMachinery = machineryService.update(id, updatedMachinery);

            var machineryResource = new MachineryResource(
                    savedMachinery.getId(),
                    savedMachinery.getProjectId(),
                    savedMachinery.getName(),
                    savedMachinery.getLicensePlate(),
                    savedMachinery.getRegisterDate(),
                    savedMachinery.getStatus()
            );

            return new ResponseEntity<>(machineryResource, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new MessageResource(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
    @Operation(summary = "Delete machinery by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResource> deleteMachinery(@PathVariable Long id) {
        machineryService.getById(id).orElseThrow(() -> new MachineryNotFoundException(id));
        machineryService.delete(id);
        return ResponseEntity.ok(new MessageResource("Machinery deleted successfully"));
    }
}
