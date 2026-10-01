package com.acme.arquitech.platform.projects.internal.queryservices;
import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.services.ProjectQueryService;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectQueryServiceImpl implements ProjectQueryService {
    private final ProjectRepository projectRepository;
    private final CurrentUserService currentUser;
    private final ProjectAccessService access;
    public List<Project> findAll() {
        var user = currentUser.get();
        return findByUserIdAndRole(user.getId(), user.getRole());
    }
    public Project findById(Long id) { return access.requireRead(id); }
    public List<Project> findByUserIdAndRole(Long id, Role role) {
        var user = currentUser.get();
        if (!user.getId().equals(id) || user.getRole() != role) throw new AccessDeniedException("Access denied");
        return switch (role) {
            case SUPERVISOR -> projectRepository.findBySupervisorId(id);
            case CONTRACTOR -> projectRepository.findByContractorId(id);
            default -> throw new AccessDeniedException("Unsupported role");
        };
    }
}
