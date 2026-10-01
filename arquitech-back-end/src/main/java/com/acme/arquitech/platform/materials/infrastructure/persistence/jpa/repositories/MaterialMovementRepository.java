package com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.materials.domain.model.aggregates.MaterialMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MaterialMovementRepository extends JpaRepository<MaterialMovement, Long> {
    List<MaterialMovement> findByMaterialProjectIdOrderByOccurredAtAscIdAsc(Long projectId);
    List<MaterialMovement> findByMaterialProjectIdAndMaterialNameOrderByOccurredAtAscIdAsc(Long projectId, String name);
    void deleteByMaterialId(Long materialId);
}
