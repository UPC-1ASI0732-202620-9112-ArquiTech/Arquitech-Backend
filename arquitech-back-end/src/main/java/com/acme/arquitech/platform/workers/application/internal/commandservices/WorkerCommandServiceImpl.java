package com.acme.arquitech.platform.workers.application.internal.commandservices;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.exceptions.WorkerNotFoundException;
import com.acme.arquitech.platform.workers.domain.services.WorkerService;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import com.acme.arquitech.platform.workers.domain.model.commands.*;
import com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@PreAuthorize("hasAuthority('SUPERVISOR')")
public class WorkerCommandServiceImpl implements WorkerService {
    private final WorkerRepository repository;
    private final TaskRepository tasks;
    private final ProjectAccessService access;
    private final com.acme.arquitech.platform.attendance.infrastructure.persistence.jpa.repositories.AttendanceRepository attendance;
    public Worker create(CreateWorkerCommand r) {
        var project = access.requireWrite(r.projectId());
        return repository.save(new Worker(project, r.fullName(), r.role(), r.specialty(), r.hireDate(), r.status()));
    }
    public Worker update(Long id, UpdateWorkerCommand r) {
        var worker = writable(id);
        if (r.projectId() != null && !worker.getProject().getId().equals(r.projectId()))
            throw ApiException.invalid("VALIDATION_ERROR", "projectId cannot be changed");
        worker.update(r.fullName(), r.role(), r.specialty(), r.hireDate(), r.status());
        return worker;
    }
    public void delete(Long id) {
        var worker = writable(id);
        if (tasks.existsByWorkerId(id)) throw ApiException.conflict("WORKER_HAS_TASKS", "Reassign or delete the worker's tasks first");
        if (attendance.existsByWorkerId(id)) throw ApiException.conflict("WORKER_HAS_ATTENDANCE", "Delete attendance records first or mark the worker inactive");
        repository.delete(worker);
    }
    private Worker writable(Long id) {
        var worker = repository.findById(id).orElseThrow(() -> new WorkerNotFoundException(id));
        access.requireWrite(worker.getProject().getId());
        return worker;
    }
}
