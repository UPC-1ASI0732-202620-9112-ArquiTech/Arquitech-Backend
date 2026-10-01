package com.acme.arquitech.platform.workers.domain.exceptions;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class WorkerNotFoundException extends ApiException {
    public WorkerNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "WORKER_NOT_FOUND", "Worker with id " + id + " was not found");
    }
}
