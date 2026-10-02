package com.acme.arquitech.platform.workers.domain.model.commands;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerStatus;
import java.time.LocalDate;

public record UpdateWorkerCommand(Long projectId, String fullName, String role, String specialty, LocalDate hireDate, WorkerStatus status) { }
