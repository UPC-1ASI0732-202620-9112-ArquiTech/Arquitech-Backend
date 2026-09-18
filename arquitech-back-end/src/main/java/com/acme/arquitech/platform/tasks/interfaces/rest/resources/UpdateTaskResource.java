package com.acme.arquitech.platform.tasks.interfaces.rest.resources;

import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;

import java.time.LocalDate;

public record UpdateTaskResource(Long projectId, Long workerId, String description, LocalDate startDate, LocalDate dueDate, TaskStatus status) {
}