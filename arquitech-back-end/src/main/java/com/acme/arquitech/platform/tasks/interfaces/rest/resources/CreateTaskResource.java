package com.acme.arquitech.platform.tasks.interfaces.rest.resources;
import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
@com.fasterxml.jackson.annotation.JsonIgnoreProperties("completedAt")
public record CreateTaskResource(
        @JsonAlias({"project_id", "id_project"}) @NotNull @Positive Long projectId,
        @JsonAlias({"worker_id", "id_worker"}) @NotNull @Positive Long workerId,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 400) String description,
        @NotNull TaskStatus status,
        @JsonAlias("due_date") @NotNull LocalDate dueDate) {
    public com.acme.arquitech.platform.tasks.domain.model.commands.CreateTaskCommand toCommand() {
        return new com.acme.arquitech.platform.tasks.domain.model.commands.CreateTaskCommand(projectId, workerId, title, description, status, dueDate);
    }
}
