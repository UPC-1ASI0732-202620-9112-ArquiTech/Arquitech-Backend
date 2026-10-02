package com.acme.arquitech.platform.incidents.internal.commandservices;
import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.domain.exceptions.IncidentNotFoundException;
import com.acme.arquitech.platform.incidents.domain.services.IncidentService;
import com.acme.arquitech.platform.incidents.repositories.IncidentRepository;
import com.acme.arquitech.platform.incidents.domain.model.commands.*;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAuthority('SUPERVISOR')")
public class IncidentCommandServiceImpl implements IncidentService {
    private final IncidentRepository repository;
    private final ProjectAccessService access;
    private final CurrentUserService currentUser;
    private final Clock clock;
    public Incident create(CreateIncidentCommand r) {
        access.requireWrite(r.projectId());
        // reportedByUserId from the request is accepted only for compatibility and never trusted.
        return repository.save(new Incident(r.projectId(), currentUser.get().getId(), r.type(), r.description(),
                r.severity(), r.status(), r.reportedAt(), OffsetDateTime.now(clock)));
    }
    public Incident update(Long id, UpdateIncidentCommand r) {
        var incident = writable(id);
        if (r.projectId() != null && !incident.getProjectId().equals(r.projectId()))
            throw ApiException.invalid("VALIDATION_ERROR", "projectId cannot be changed");
        incident.update(r.type(), r.description(), r.severity(), r.status(), OffsetDateTime.now(clock));
        incident.correctReportedAt(r.reportedAt());
        return incident;
    }
    public void delete(Long id) { repository.delete(writable(id)); }
    private Incident writable(Long id) {
        var incident = repository.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
        access.requireWrite(incident.getProjectId());
        return incident;
    }
}
