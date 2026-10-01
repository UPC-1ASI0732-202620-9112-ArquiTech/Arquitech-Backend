package com.acme.arquitech.platform.tasks.interfaces.rest.resources;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;
import java.time.LocalDate;
public record TaskResource(Long id, Long projectId, Long workerId, String workerName, String title, String description, TaskStatus status, LocalDate dueDate, java.util.Date createdAt, java.time.OffsetDateTime completedAt) {
    public static TaskResource from(Task m) {
        return new TaskResource(m.getId(), m.getProject().getId(), m.getWorker().getId(), m.getWorker().getFullName().value(), m.getTitle(), m.getDescription(), m.getStatus().canonical(), m.getDueDate(), m.getCreatedAt(), m.getCompletedAt());
    }
}
