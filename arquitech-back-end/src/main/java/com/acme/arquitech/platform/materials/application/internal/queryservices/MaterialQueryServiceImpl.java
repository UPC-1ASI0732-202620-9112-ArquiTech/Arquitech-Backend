package com.acme.arquitech.platform.materials.application.internal.queryservices;

import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import com.acme.arquitech.platform.materials.domain.service.MaterialService;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.MaterialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MaterialQueryServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;

    public MaterialQueryServiceImpl(MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    @Override
    public Material createMaterial(Material material) {
        throw new UnsupportedOperationException("Use command service for create operations");
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
        throw new UnsupportedOperationException("Use command service for update operations");
    }

    @Override
    public List<Material> getTransactionHistory(Long projectId, String materialName) {
        return materialRepository.findAllByProjectIdAndName(projectId, materialName);
    }

    @Override
    public boolean isLowInventory(Long materialId, Integer minimumLevel) {
        return materialRepository.findById(materialId)
                .map(material -> (material.getQuantity() - material.getQuantityExit()) < minimumLevel)
                .orElse(false);
    }
    public List<Material> findAll() {
        return materialRepository.findAll();
    }
}