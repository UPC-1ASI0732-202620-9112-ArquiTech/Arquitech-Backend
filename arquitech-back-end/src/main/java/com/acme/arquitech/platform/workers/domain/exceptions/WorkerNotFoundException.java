package com.acme.arquitech.platform.workers.domain.exceptions;

public class WorkerNotFoundException extends RuntimeException {
    public WorkerNotFoundException(Long id) {
        super("Worker with ID " + id + " not found");
    }
}