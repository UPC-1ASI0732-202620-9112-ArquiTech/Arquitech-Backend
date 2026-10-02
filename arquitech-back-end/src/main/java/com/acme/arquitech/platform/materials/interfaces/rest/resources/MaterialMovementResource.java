package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import com.acme.arquitech.platform.materials.domain.model.aggregates.MaterialMovement;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MovementType;
import java.time.OffsetDateTime;
public record MaterialMovementResource(Long id, Long materialId, Long projectId, String materialName, String unit,
        MovementType type, Integer quantity, String supplier, Long registeredByUserId,
        String registeredByName, OffsetDateTime occurredAt, String note) {
    public static MaterialMovementResource from(MaterialMovement m) {
        return new MaterialMovementResource(m.getId(), m.getMaterial().getId(), m.getMaterial().getProjectId(),
                m.getMaterial().getName(), m.getMaterial().getUnit(), m.getType(), m.getQuantity(), m.getSupplier(),
                m.getRegisteredBy().getId(), m.getRegisteredBy().getName(), m.getOccurredAt(), m.getNote());
    }
}
