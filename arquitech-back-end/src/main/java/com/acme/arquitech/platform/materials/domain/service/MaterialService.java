package com.acme.arquitech.platform.materials.domain.service;

import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;

import java.util.List;
import java.util.Optional;

public interface MaterialService {
    Material createMaterial(Material material);
    Optional<Material> findById(Long id);
    List<Material> findAllByProjectId(Long projectId);
    Material useMaterial(Long materialId, Integer quantity, String exitDate);
    List<Material> getTransactionHistory(Long projectId, String materialName);
    boolean isLowInventory(Long materialId, Integer minimumLevel);
}