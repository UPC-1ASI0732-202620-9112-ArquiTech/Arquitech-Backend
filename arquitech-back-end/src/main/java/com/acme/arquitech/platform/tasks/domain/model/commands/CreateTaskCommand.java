package com.acme.arquitech.platform.tasks.domain.model.commands;
import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;
import java.time.LocalDate;

public record CreateTaskCommand(Long projectId, Long workerId, String title, String description, TaskStatus status, LocalDate dueDate) { }
