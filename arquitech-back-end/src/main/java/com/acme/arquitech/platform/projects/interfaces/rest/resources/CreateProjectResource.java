package com.acme.arquitech.platform.projects.interfaces.rest.resources;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProjectResource(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String location,
        @JsonAlias("start_date") @NotNull LocalDate startDate,
        @JsonAlias("end_date") @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0") @Digits(integer = 8, fraction = 2) BigDecimal budget,
        @NotNull ProjectStatus status,
        @NotNull @Min(0) @Max(100) Integer progress,
        @JsonAlias({"userId", "user_id", "supervisor_id"}) @NotNull @Positive Long supervisorId,
        @JsonAlias("contractor_id") @NotNull @Positive Long contractorId,
        @JsonAlias("image_url") @Size(max = 255) String imageUrl) {
    public com.acme.arquitech.platform.projects.domain.model.commands.CreateProjectCommand toCommand() {
        return new com.acme.arquitech.platform.projects.domain.model.commands.CreateProjectCommand(name, location, startDate, endDate, budget, status, progress, supervisorId, contractorId, imageUrl);
    }
}
