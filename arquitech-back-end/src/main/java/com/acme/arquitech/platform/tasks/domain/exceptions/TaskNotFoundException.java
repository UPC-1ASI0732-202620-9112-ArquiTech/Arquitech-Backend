package com.acme.arquitech.platform.tasks.domain.exceptions;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class TaskNotFoundException extends ApiException {
    public TaskNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "Task with id " + id + " was not found");
    }
}
