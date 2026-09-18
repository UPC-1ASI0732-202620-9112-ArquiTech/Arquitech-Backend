package com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories;

import com.acme.arquitech.platform.materials.domain.model.aggregates.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findAllByProjectId(Long projectId);
    List<Material> findAllByProjectIdAndName(Long projectId, String name);
}