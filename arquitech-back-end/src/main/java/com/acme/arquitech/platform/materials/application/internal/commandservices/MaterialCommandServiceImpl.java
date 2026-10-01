package com.acme.arquitech.platform.materials.application.internal.commandservices;
import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.materials.domain.exception.MaterialNotFoundException;
import com.acme.arquitech.platform.materials.domain.model.aggregates.*;
import com.acme.arquitech.platform.materials.domain.model.valueobjects.MovementType;
import com.acme.arquitech.platform.materials.domain.service.MaterialService;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.*;
import com.acme.arquitech.platform.materials.domain.model.commands.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAuthority('SUPERVISOR')")
public class MaterialCommandServiceImpl implements MaterialService {
    private final MaterialRepository materialRepository;
    private final MaterialMovementRepository movements;
    private final ProjectAccessService access;
    private final CurrentUserService currentUser;

    public Material createMaterial(CreateMaterialCommand r) {
        access.requireWrite(r.projectId());
        var material = materialRepository.save(new Material(r.projectId(), r.name(), r.unit(), r.quantity(),
                r.minimumStock(), r.unitPrice(), r.provider(), r.providerRuc(), r.date()));
        movements.save(new MaterialMovement(material, MovementType.ENTRY, r.quantity(), r.provider(),
                currentUser.get(), r.date().atStartOfDay().atOffset(ZoneOffset.UTC), "Initial inventory"));
        return material;
    }

    public Material updateMaterial(Long id, UpdateMaterialCommand r) {
        var material = writable(id);
        material.updateDetails(r.name(), r.unit(), r.minimumStock(), r.unitPrice(), r.provider(), r.providerRuc());
        return material;
    }

    public MaterialMovement enter(Long id, MaterialEntryCommand r) {
        var material = writable(id);
        material.enter(r.quantity(), r.occurredAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate());
        return movements.save(new MaterialMovement(material, MovementType.ENTRY, r.quantity(), r.supplier(),
                currentUser.get(), r.occurredAt(), r.note()));
    }

    public MaterialMovement use(Long id, MaterialUsageCommand r) {
        var material = writable(id);
        material.use(r.quantity(), r.occurredAt().withOffsetSameInstant(ZoneOffset.UTC).toLocalDate());
        return movements.save(new MaterialMovement(material, MovementType.USAGE, r.quantity(), null,
                currentUser.get(), r.occurredAt(), r.note()));
    }

    public void delete(Long id) {
        var material = writable(id);
        // Explicit cascade within the same transaction: history is removed before its parent.
        movements.deleteByMaterialId(id);
        movements.flush();
        materialRepository.delete(material);
    }

    private Material writable(Long id) {
        var material = materialRepository.findForUpdate(id).orElseThrow(() -> new MaterialNotFoundException(id));
        access.requireWrite(material.getProjectId());
        return material;
    }
}
