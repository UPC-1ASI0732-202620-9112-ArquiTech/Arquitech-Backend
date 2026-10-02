package com.acme.arquitech.platform.workers.application.internal.queryservices;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.exceptions.WorkerNotFoundException;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WorkerQueryServiceImpl {
    private final WorkerRepository repository;
    private final ProjectAccessService access;
    public Worker findById(Long id) {
        var entity = repository.findById(id).orElseThrow(() -> new WorkerNotFoundException(id));
        access.requireRead(entity.getProject().getId());
        return entity;
    }
    public List<Worker> findAll(Long projectId) {
        var scope = access.scope(projectId);
        return scope.isEmpty() ? List.of() : repository.findByProjectIdIn(scope);
    }
}
