package com.acme.arquitech.platform.projects.application.authorization;

import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.projects.domain.exceptions.ProjectNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectAccessService {
    private final ProjectRepository projects;
    private final CurrentUserService currentUser;

    public Project requireRead(Long id) {
        var project = projects.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        var user = currentUser.get();
        boolean allowed = user.getRole() == Role.SUPERVISOR && project.getSupervisor().getId().equals(user.getId())
                || user.getRole() == Role.CONTRACTOR && project.getContractor().getId().equals(user.getId());
        if (!allowed) throw new AccessDeniedException("Access denied");
        return project;
    }

    public Project requireWrite(Long id) {
        currentUser.supervisor();
        return requireRead(id);
    }

    public List<Long> accessibleProjectIds() {
        var user = currentUser.get();
        return switch (user.getRole()) {
            case SUPERVISOR -> projects.findIdsBySupervisorId(user.getId());
            case CONTRACTOR -> projects.findIdsByContractorId(user.getId());
            default -> throw new AccessDeniedException("Unsupported role");
        };
    }

    public List<Long> scope(Long projectId) {
        if (projectId == null) return accessibleProjectIds();
        requireRead(projectId);
        return List.of(projectId);
    }
}
