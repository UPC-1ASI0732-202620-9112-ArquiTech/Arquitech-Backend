package com.acme.arquitech.platform.materials.application.internal.commandservices;

import com.acme.arquitech.platform.materials.domain.exception.InsufficientStockException;
import com.acme.arquitech.platform.materials.domain.exception.InvalidMaterialDataException;
import com.acme.arquitech.platform.materials.domain.exception.MaterialNotFoundException;
import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MaterialStatus;
import com.acme.arquitech.platform.materials.domain.service.MaterialService;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.MaterialRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MaterialCommandServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialCommandServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public Material createMaterial(Material material) {
        validateMaterial(material);
        material.setStatus(MaterialStatus.RECEIVED);
        return materialRepository.save(material);
    }

    @Override
    public Optional<Material> findById(Long id) {
        return materialRepository.findById(id);
    }

    @Override
    public List<Material> findAllByProjectId(Long projectId) {
        return materialRepository.findAllByProjectId(projectId);
    }

    @Override
    public Material useMaterial(Long materialId, Integer quantity, String exitDate) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));
        material.useMaterial(quantity, exitDate);
        return materialRepository.save(material);
    }

    @Override
    public List<Material> getTransactionHistory(Long projectId, String materialName) {
        return materialRepository.findAllByProjectIdAndName(projectId, materialName);
    }

    @Override
    public boolean isLowInventory(Long materialId, Integer minimumLevel) {
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new MaterialNotFoundException(materialId));
        return (material.getQuantity() - material.getQuantityExit()) < minimumLevel;
    }

    private void validateMaterial(Material material) {
        if (Objects.isNull(material.getQuantity()) || material.getQuantity() < 0) {
            throw new InvalidMaterialDataException("Quantity is required and must be non-negative");
        }
        if (Objects.isNull(material.getUnitPrice()) || material.getUnitPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMaterialDataException("Unit price is required and must be non-negative");
        }
        if (material.getName() == null || material.getName().isBlank()) {
            throw new InvalidMaterialDataException("Material name is required");
        }
    }
    public Material updateMaterial(
            Long id,
            String name,
            Integer quantity,
            BigDecimal unitPrice,
            String unit,
            String provider,
            String providerRuc,
            String date,
            String receiptNumber,
            String paymentMethod,
            String entryType,
            String exitType,
            String exitDate
    ) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new MaterialNotFoundException(id));
        material.setName(name);
        material.setQuantity(quantity);
        material.setUnitPrice(unitPrice);
        material.setUnit(unit);
        material.setProvider(provider);
        material.setProviderRuc(providerRuc);
        material.setDate(date);
        material.setReceiptNumber(receiptNumber);
        material.setPaymentMethod(paymentMethod);
        material.setEntryType(entryType);
        material.setExitType(exitType);
        material.setExitDate(exitDate);

        return materialRepository.save(material);
    }

}