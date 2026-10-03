package com.acme.arquitech.platform.tasks.domain.model.aggregates;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.*;

@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor
public class Task extends AuditableAbstractAggregateRoot<Task> {
    @ManyToOne @JoinColumn(name = "id_project", nullable = false)
    private Project project;
    @ManyToOne @JoinColumn(name = "id_worker", nullable = false)
    private Worker worker;
    private String title;
    @Column(nullable = false, length = 400)
    private String description;
    // Required by existing databases; retained outside the public contract.
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskStatus status;
    private OffsetDateTime completedAt;

    public Task(Project project, Worker worker, String title, String description, LocalDate dueDate,
                TaskStatus status, OffsetDateTime now) {
        this.project = project;
        this.startDate = now.toLocalDate();
        update(worker, title, description, dueDate, status, now);
    }
    public void update(Worker worker, String title, String description, LocalDate dueDate,
                       TaskStatus status, OffsetDateTime now) {
        if (!worker.getProject().getId().equals(project.getId()))
            throw ApiException.invalid("VALIDATION_ERROR", "Worker must belong to the task project");
        // Keep deserializing the legacy enum value so clients receive a controlled 400 response.
        if ("DONE".equals(status.name())) throw ApiException.invalid("VALIDATION_ERROR", "Use COMPLETED");
        if (status == TaskStatus.COMPLETED) {
            if (this.status == null || this.status.canonical() != TaskStatus.COMPLETED) completedAt = now;
        } else completedAt = null;
        this.worker = worker;
        this.title = title;
        this.description = description == null ? "" : description;
        this.dueDate = dueDate;
        this.status = status;
    }
    public String getTitle() { return title == null ? description : title; }
}
