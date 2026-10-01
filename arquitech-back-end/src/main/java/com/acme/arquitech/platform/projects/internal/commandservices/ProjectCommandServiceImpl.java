package com.acme.arquitech.platform.projects.internal.commandservices;
import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import com.acme.arquitech.platform.projects.domain.services.ProjectCommandService;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.projects.domain.model.commands.CreateProjectCommand;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectCommandServiceImpl implements ProjectCommandService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUser;
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public Project create(CreateProjectCommand r) {
        var supervisor = currentUser.supervisor();
        if (!supervisor.getId().equals(r.supervisorId())) throw new AccessDeniedException("Cannot create a project for another supervisor");
        var contractor = userRepository.findById(r.contractorId())
                .filter(user -> user.getRole() == Role.CONTRACTOR)
                .orElseThrow(() -> ApiException.invalid("INVALID_CONTRACTOR", "A valid contractor is required"));
        if (r.status() == ProjectStatus.PAUSED) throw ApiException.invalid("VALIDATION_ERROR", "Use SUSPENDED");
        return projectRepository.save(new Project(r.name(), r.location(), r.startDate(), r.endDate(), r.budget(),
                r.status(), r.progress(), supervisor, contractor, r.imageUrl()));
    }
}
