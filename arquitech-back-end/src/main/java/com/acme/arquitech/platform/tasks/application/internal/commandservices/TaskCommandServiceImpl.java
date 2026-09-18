package com.acme.arquitech.platform.tasks.application.internal.commandservices;

import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.tasks.domain.exceptions.TaskNotFoundException;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import com.acme.arquitech.platform.tasks.domain.services.TaskService;
import com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskCommandServiceImpl implements TaskService {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final WorkerRepository workerRepository;

    public TaskCommandServiceImpl(TaskRepository taskRepository, ProjectRepository projectRepository, WorkerRepository workerRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.workerRepository = workerRepository;
    }

    @Override
    public Task create(Task task) {
        Project project = projectRepository.findById(task.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Worker worker = workerRepository.findById(task.getWorker().getId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        task.setProject(project);
        task.setWorker(worker);
        return taskRepository.save(task);
    }

    @Override
    public Task update(Task task) {
        Task existingTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new TaskNotFoundException(task.getId()));
        existingTask.setDescription(task.getDescription());
        existingTask.setStartDate(task.getStartDate());
        existingTask.setDueDate(task.getDueDate());
        existingTask.setStatus(task.getStatus());
        Project project = projectRepository.findById(task.getProject().getId())
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Worker worker = workerRepository.findById(task.getWorker().getId())
                .orElseThrow(() -> new RuntimeException("Worker not found"));
        existingTask.setProject(project);
        existingTask.setWorker(worker);
        return taskRepository.save(existingTask);
    }

    @Override
    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public List<Task> findByWorkerIdAndProjectId(Long workerId, Long projectId) {
        return taskRepository.findByWorkerIdAndProjectId(workerId, projectId);
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

}