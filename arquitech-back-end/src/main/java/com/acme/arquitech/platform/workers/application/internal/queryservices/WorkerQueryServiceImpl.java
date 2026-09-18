package com.acme.arquitech.platform.workers.application.internal.queryservices;

import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.workers.application.internal.commandservices.WorkerCommandServiceImpl;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.services.WorkerService;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkerQueryServiceImpl implements WorkerService {
    private final WorkerRepository workerRepository;
    @Getter
    private final ProjectRepository projectRepository;
    private final WorkerCommandServiceImpl workerCommandService;

    public WorkerQueryServiceImpl(WorkerRepository workerRepository, ProjectRepository projectRepository, WorkerCommandServiceImpl workerCommandService) {
        this.workerRepository = workerRepository;
        this.projectRepository = projectRepository;
        this.workerCommandService = workerCommandService;
    }

    @Override
    public Worker create(Worker worker) {
        return workerCommandService.create(worker);
    }

    @Override
    public Worker update(Worker worker) {
        return workerCommandService.update(worker);
    }

    @Override
    public void delete(Long id) {
        workerCommandService.delete(id);
    }

    @Override
    public List<Worker> findByProjectId(Long projectId) {
        return workerRepository.findByProjectId(projectId);
    }

    @Override
    public Optional<Worker> findById(Long id) {
        return workerRepository.findById(id);
    }

}