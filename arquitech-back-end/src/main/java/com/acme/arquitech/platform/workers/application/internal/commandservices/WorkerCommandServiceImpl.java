package com.acme.arquitech.platform.workers.application.internal.commandservices;

import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.workers.domain.exceptions.WorkerNotFoundException;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.services.WorkerService;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WorkerCommandServiceImpl implements WorkerService {
    private final WorkerRepository workerRepository;
    private final ProjectRepository projectRepository;

    public WorkerCommandServiceImpl(WorkerRepository workerRepository, ProjectRepository projectRepository) {
        this.workerRepository = workerRepository;
        this.projectRepository = projectRepository;
    }

    @Override
    public Worker create(Worker worker) {
        Project project = projectRepository.findById(worker.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        worker.setProject(project);
        return workerRepository.save(worker);
    }

    @Override
    public Worker update(Worker worker) {
        Worker existingWorker = workerRepository.findById(worker.getId())
                .orElseThrow(() -> new WorkerNotFoundException(worker.getId()));
        existingWorker.setName(worker.getName());
        existingWorker.setRole(worker.getRole());
        existingWorker.setHiredDate(worker.getHiredDate());
        Project project = projectRepository.findById(worker.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        existingWorker.setProject(project);
        return workerRepository.save(existingWorker);
    }

    @Override
    public void delete(Long id) {
        if (!workerRepository.existsById(id)) {
            throw new WorkerNotFoundException(id);
        }
        workerRepository.deleteById(id);
    }

    @Override
    public List<Worker> findByProjectId(Long projectId) {
        return workerRepository.findByProjectId(projectId);
    }

    @Override
    public Optional<Worker> findById(Long id) {
        return workerRepository.findById(id);
    }

    public List<Worker> getAll() {
        return workerRepository.findAll();
    }
}