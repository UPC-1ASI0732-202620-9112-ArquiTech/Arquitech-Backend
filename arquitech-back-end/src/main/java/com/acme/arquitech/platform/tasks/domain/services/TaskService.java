package com.acme.arquitech.platform.tasks.domain.services;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import com.acme.arquitech.platform.tasks.domain.model.commands.*;
public interface TaskService {
    Task create(CreateTaskCommand resource);
    Task update(Long id, UpdateTaskCommand resource);
    void delete(Long id);
}
