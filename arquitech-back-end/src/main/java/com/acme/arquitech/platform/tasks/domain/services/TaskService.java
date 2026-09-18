package com.acme.arquitech.platform.tasks.domain.services;

import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    Task create(Task task);
    Task update(Task task);
    void delete(Long id);
    List<Task> findByWorkerIdAndProjectId(Long workerId, Long projectId);
    Optional<Task> findById(Long id);
}