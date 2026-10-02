package com.acme.arquitech.platform.materials.application.internal.queryservices;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.materials.domain.exception.MaterialNotFoundException;
import com.acme.arquitech.platform.materials.domain.model.aggregates.*;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialQueryServiceImpl {
    private final MaterialRepository materialRepository;
    private final MaterialMovementRepository movements;
    private final ProjectAccessService access;
    public Material findById(Long id) {
        var material = materialRepository.findById(id).orElseThrow(() -> new MaterialNotFoundException(id));
        access.requireRead(material.getProjectId());
        return material;
    }
    public List<Material> findAll(Long projectId) {
        var scope = access.scope(projectId);
        return scope.isEmpty() ? List.of() : materialRepository.findByProjectIdIn(scope);
    }
    public List<MaterialMovement> history(Long projectId, String materialName) {
        access.requireRead(projectId);
        return materialName == null ? movements.findByMaterialProjectIdOrderByOccurredAtAscIdAsc(projectId)
                : movements.findByMaterialProjectIdAndMaterialNameOrderByOccurredAtAscIdAsc(projectId, materialName);
    }
    public boolean isLowInventory(Long id, Integer minimumLevel) {
        var material = findById(id);
        return material.getStock() < (minimumLevel == null ? material.getMinimumStock() : minimumLevel);
    }
}
