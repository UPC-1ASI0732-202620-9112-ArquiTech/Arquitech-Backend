package com.acme.arquitech.platform.tasks.application.internal.queryservices;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import com.acme.arquitech.platform.tasks.domain.exceptions.TaskNotFoundException;
import com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskQueryServiceImpl {
    private final TaskRepository repository;
    private final ProjectAccessService access;
    public Task findById(Long id) {
        var entity = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        access.requireRead(entity.getProject().getId());
        return entity;
    }
    public List<Task> findAll(Long projectId) {
        var scope = access.scope(projectId);
        return scope.isEmpty() ? List.of() : repository.findByProjectIdIn(scope);
    }
}
