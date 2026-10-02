package com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByProjectIdIn(Collection<Long> projectIds);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> findForUpdate(@Param("id") Long id);
}
