package com.acme.arquitech.platform.tasks.application.internal.commandservices;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import com.acme.arquitech.platform.tasks.domain.exceptions.TaskNotFoundException;
import com.acme.arquitech.platform.tasks.domain.services.TaskService;
import com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import com.acme.arquitech.platform.tasks.domain.model.commands.*;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.exceptions.WorkerNotFoundException;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAuthority('SUPERVISOR')")
public class TaskCommandServiceImpl implements TaskService {
    private final TaskRepository repository;
    private final WorkerRepository workers;
    private final ProjectAccessService access;
    private final Clock clock;
    public Task create(CreateTaskCommand r) {
        var project = access.requireWrite(r.projectId());
        var worker = worker(r.workerId(), project.getId());
        return repository.save(new Task(project, worker, r.title(), r.description(), r.dueDate(), r.status(), OffsetDateTime.now(clock)));
    }
    public Task update(Long id, UpdateTaskCommand r) {
        var task = writable(id);
        if (r.projectId() != null && !task.getProject().getId().equals(r.projectId()))
            throw ApiException.invalid("VALIDATION_ERROR", "projectId cannot be changed");
        task.update(worker(r.workerId(), task.getProject().getId()), r.title(), r.description(), r.dueDate(), r.status(), OffsetDateTime.now(clock));
        return task;
    }
    public void delete(Long id) { repository.delete(writable(id)); }
    private Task writable(Long id) {
        var task = repository.findById(id).orElseThrow(() -> new TaskNotFoundException(id));
        access.requireWrite(task.getProject().getId());
        return task;
    }
    private Worker worker(Long id, Long projectId) {
        var worker = workers.findById(id).orElseThrow(() -> new WorkerNotFoundException(id));
        access.requireRead(worker.getProject().getId());
        if (!worker.getProject().getId().equals(projectId))
            throw ApiException.invalid("VALIDATION_ERROR", "Worker must belong to the task project");
        return worker;
    }
}
