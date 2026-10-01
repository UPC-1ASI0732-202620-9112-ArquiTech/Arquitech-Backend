package com.acme.arquitech.platform.materials.domain.service;
import com.acme.arquitech.platform.materials.domain.model.aggregates.*;
import com.acme.arquitech.platform.materials.domain.model.commands.*;
public interface MaterialService {
    Material createMaterial(CreateMaterialCommand resource);
    Material updateMaterial(Long id, UpdateMaterialCommand resource);
    MaterialMovement enter(Long id, MaterialEntryCommand resource);
    MaterialMovement use(Long id, MaterialUsageCommand resource);
    void delete(Long id);
}
