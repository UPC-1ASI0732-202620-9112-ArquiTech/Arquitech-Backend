package com.acme.arquitech.platform.machinery.application.internal.queryservices;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import com.acme.arquitech.platform.machinery.domain.exception.MachineryNotFoundException;
import com.acme.arquitech.platform.machinery.infrastructure.persistence.jpa.repositories.MachineryRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MachineryQueryServiceImpl {
    private final MachineryRepository repository;
    private final ProjectAccessService access;
    public Machinery findById(Long id) {
        var entity = repository.findById(id).orElseThrow(() -> new MachineryNotFoundException(id));
        access.requireRead(entity.getProjectId());
        return entity;
    }
    public List<Machinery> findAll(Long projectId) {
        var scope = access.scope(projectId);
        return scope.isEmpty() ? List.of() : repository.findByProjectIdIn(scope);
    }
}
