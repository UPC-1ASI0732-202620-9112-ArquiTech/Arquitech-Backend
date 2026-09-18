package com.acme.arquitech.platform.materials.interfaces.rest;

import com.acme.arquitech.platform.materials.application.internal.commandservices.MaterialCommandServiceImpl;
import com.acme.arquitech.platform.materials.application.internal.queryservices.MaterialQueryServiceImpl;
import com.acme.arquitech.platform.materials.domain.exception.InsufficientStockException;
import com.acme.arquitech.platform.materials.domain.exception.InvalidMaterialDataException;
import com.acme.arquitech.platform.materials.domain.exception.MaterialNotFoundException;
import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MaterialStatus;
import com.acme.arquitech.platform.materials.interfaces.rest.resources.CreateMaterialResource;
import com.acme.arquitech.platform.materials.interfaces.rest.resources.MaterialResource;
import com.acme.arquitech.platform.materials.interfaces.rest.resources.MaterialTransactionResource;
import com.acme.arquitech.platform.materials.interfaces.rest.resources.UpdateMaterialResource;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/materials")
@Tag(name = "Materials", description = "Material Management Endpoints")
public class MaterialController {

    private final MaterialCommandServiceImpl commandService;
    private final MaterialQueryServiceImpl queryService;

    public MaterialController(MaterialCommandServiceImpl commandService, MaterialQueryServiceImpl queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<MaterialResource> createMaterial(@RequestBody CreateMaterialResource resource) {
        Material material = new Material(
                resource.projectId(),
                resource.name(),
                resource.quantity(),
                resource.unitPrice(),
                resource.unit(),
                resource.provider(),
                resource.providerRuc(),
                resource.date(),
                resource.receiptNumber(),
                resource.paymentMethod(),
                MaterialStatus.RECEIVED,
                0,
                resource.entryType(),
                null,
                null
        );
        Material savedMaterial = commandService.createMaterial(material);
        return new ResponseEntity<>(toResource(savedMaterial), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<MaterialResource>> getAllMaterials() {
        List<MaterialResource> resources = queryService.findAll()
                .stream()
                .map(this::toResource)
                .collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<MaterialResource> getMaterial(@PathVariable Long id) {
        return queryService.findById(id)
                .map(material -> new ResponseEntity<>(toResource(material), HttpStatus.OK))
                .orElseThrow(() -> new MaterialNotFoundException(id));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<MaterialResource>> getMaterialsByProject(@PathVariable Long projectId) {
        List<MaterialResource> resources = queryService.findAllByProjectId(projectId)
                .stream().map(this::toResource).collect(Collectors.toList());
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @PostMapping("/{id}/use")
    public ResponseEntity<MaterialResource> useMaterial(@PathVariable Long id, @RequestBody UpdateMaterialResource resource) {
        Material updatedMaterial = commandService.useMaterial(id, resource.quantity(), String.valueOf(resource.exitDate()));
        return new ResponseEntity<>(toResource(updatedMaterial), HttpStatus.OK);
    }

    @GetMapping("/project/{projectId}/history/{materialName}")
    public ResponseEntity<List<MaterialTransactionResource>> getTransactionHistory(@PathVariable Long projectId, @PathVariable String materialName) {
        List<MaterialTransactionResource> resources = queryService.getTransactionHistory(projectId, materialName)
                .stream().map(this::toTransactionResource).collect(Collectors.toList());
        if (resources.isEmpty()) {
            return new ResponseEntity<>(List.of(), HttpStatus.OK);
        }
        return new ResponseEntity<>(resources, HttpStatus.OK);
    }

    @GetMapping("/{id}/low-inventory")
    public ResponseEntity<MessageResource> checkLowInventory(@PathVariable Long id, @RequestParam Integer minimumLevel) {
        boolean isLow = queryService.isLowInventory(id, minimumLevel);
        String message = isLow ? "Material stock is below minimum level" : "Material stock is sufficient";
        return new ResponseEntity<>(new MessageResource(message), HttpStatus.OK);
    }

    private MaterialResource toResource(Material material) {
        return new MaterialResource(
                material.getId(),
                material.getProjectId(),
                material.getName(),
                material.getQuantity(),
                material.getUnitPrice(),
                material.getUnit(),
                material.getProvider(),
                material.getProviderRuc(),
                material.getDate(),
                material.getReceiptNumber(),
                material.getPaymentMethod(),
                material.getStatus().name(),
                material.getQuantityExit(),
                material.getEntryType(),
                material.getExitType(),
                material.getExitDate()
        );
    }

    private MaterialTransactionResource toTransactionResource(Material material) {
        return new MaterialTransactionResource(
                material.getId(),
                material.getProjectId(),
                material.getName(),
                material.getQuantity(),
                material.getProvider(),
                material.getDate(),
                material.getEntryType(),
                material.getExitType(),
                material.getExitDate()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<MaterialResource> updateMaterial(
            @PathVariable Long id,
            @RequestBody UpdateMaterialResource resource) {
        try {
            Material updatedMaterial = commandService.updateMaterial(
                    id,
                    resource.name(),
                    resource.quantity(),
                    resource.unitPrice(),
                    resource.unit(),
                    resource.provider(),
                    resource.providerRuc(),
                    resource.date(),
                    resource.receiptNumber(),
                    resource.paymentMethod(),
                    resource.entryType(),
                    resource.exitType(),
                    resource.exitDate()
            );
            return new ResponseEntity<>(toResource(updatedMaterial), HttpStatus.OK);
        } catch (MaterialNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (InvalidMaterialDataException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    @ExceptionHandler(MaterialNotFoundException.class)
    public ResponseEntity<MessageResource> handleMaterialNotFound(MaterialNotFoundException ex) {
        return new ResponseEntity<>(new MessageResource(ex.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<MessageResource> handleInsufficientStock(InsufficientStockException ex) {
        return new ResponseEntity<>(new MessageResource(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidMaterialDataException.class)
    public ResponseEntity<MessageResource> handleInvalidMaterialData(InvalidMaterialDataException ex) {
        return new ResponseEntity<>(new MessageResource(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }
}