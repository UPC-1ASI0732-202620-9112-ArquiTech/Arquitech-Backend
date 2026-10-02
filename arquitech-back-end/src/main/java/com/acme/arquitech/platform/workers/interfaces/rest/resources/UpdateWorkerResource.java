package com.acme.arquitech.platform.workers.interfaces.rest.resources;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record UpdateWorkerResource(
        @JsonAlias({"project_id", "id_project"}) @Positive Long projectId,
        @JsonAlias("name") @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Size(max = 50) String role,
        @Size(max = 255) String specialty,
        @JsonAlias({"hiredDate", "hired_date"}) @NotNull LocalDate hireDate,
        @NotNull WorkerStatus status) {
    public com.acme.arquitech.platform.workers.domain.model.commands.UpdateWorkerCommand toCommand() {
        return new com.acme.arquitech.platform.workers.domain.model.commands.UpdateWorkerCommand(projectId, fullName, role, specialty, hireDate, status);
    }
}
