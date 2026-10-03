package com.acme.arquitech.platform.machinery.application.internal.commandservices;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import com.acme.arquitech.platform.machinery.domain.exception.MachineryNotFoundException;
import com.acme.arquitech.platform.machinery.domain.service.MachineryService;
import com.acme.arquitech.platform.machinery.infrastructure.persistence.jpa.repositories.MachineryRepository;
import com.acme.arquitech.platform.machinery.domain.model.commands.*;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAuthority('SUPERVISOR')")
public class MachineryCommandServiceImpl implements MachineryService {
    private final MachineryRepository repository;
    private final ProjectAccessService access;
    public Machinery create(CreateMachineryCommand r) {
        access.requireWrite(r.projectId());
        if (repository.existsByProjectIdAndSerialNumber(r.projectId(), r.serialNumber().trim())) throw duplicate();
        return save(new Machinery(r.projectId(), r.name(), r.serialNumber(), r.registeredAt(), r.status(), r.description()));
    }
    public Machinery update(Long id, UpdateMachineryCommand r) {
        var entity = writable(id);
        if (r.projectId() != null && !entity.getProjectId().equals(r.projectId()))
            throw ApiException.invalid("VALIDATION_ERROR", "projectId cannot be changed");
        if (repository.existsByProjectIdAndSerialNumberAndIdNot(
                entity.getProjectId(), r.serialNumber().trim(), id)) throw duplicate();
        entity.update(r.name(), r.serialNumber(), r.registeredAt(), r.status(), r.description());
        return save(entity);
    }
    public void delete(Long id) { repository.delete(writable(id)); }
    private Machinery writable(Long id) {
        var entity = repository.findById(id).orElseThrow(() -> new MachineryNotFoundException(id));
        access.requireWrite(entity.getProjectId());
        return entity;
    }
    private Machinery save(Machinery entity) {
        try {
            return repository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException ex) {
            throw duplicate();
        }
    }
    private ApiException duplicate() {
        return ApiException.conflict("DUPLICATED_SERIAL_NUMBER",
                "A machinery with this serial number already exists in the project");
    }
}
