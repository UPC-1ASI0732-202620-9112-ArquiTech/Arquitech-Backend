package com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Material a where a.projectId = :projectId")
    void deleteAllByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    List<Material> findByProjectIdIn(Collection<Long> projectIds);
    @Query("select m.projectId from Material m where m.id = :id")
    Optional<Long> findProjectIdById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> findForUpdate(@Param("id") Long id);
}
