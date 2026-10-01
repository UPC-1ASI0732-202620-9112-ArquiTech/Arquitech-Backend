package com.acme.arquitech.platform.workers.domain.services;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.model.commands.*;
public interface WorkerService {
    Worker create(CreateWorkerCommand resource);
    Worker update(Long id, UpdateWorkerCommand resource);
    void delete(Long id);
}
