package com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.materials.domain.model.aggregates.MaterialMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
public interface MaterialMovementRepository extends JpaRepository<MaterialMovement, Long> {
    @Lock(LockModeType.PESSIMISTIC_READ)
    Optional<MaterialMovement> findByMaterialIdAndClientRequestId(Long materialId, String clientRequestId);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from MaterialMovement m where m.material.id in (select a.id from Material a where a.projectId = :projectId)")
    void deleteAllByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    List<MaterialMovement> findByMaterialProjectIdOrderByOccurredAtAscIdAsc(Long projectId);
    List<MaterialMovement> findByMaterialProjectIdAndMaterialNameOrderByOccurredAtAscIdAsc(Long projectId, String name);
    void deleteByMaterialId(Long materialId);
}
