package com.acme.arquitech.platform.workers.domain.services;

import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;

import java.util.List;
import java.util.Optional;

public interface WorkerService {
    Worker create(Worker worker);
    Worker update(Worker worker);
    void delete(Long id);
    List<Worker> findByProjectId(Long projectId);
    Optional<Worker> findById(Long id);
}
