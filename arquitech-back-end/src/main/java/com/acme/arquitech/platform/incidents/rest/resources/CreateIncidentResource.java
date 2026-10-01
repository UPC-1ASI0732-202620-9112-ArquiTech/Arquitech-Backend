package com.acme.arquitech.platform.incidents.rest.resources;
import com.acme.arquitech.platform.incidents.domain.model.valueobjects.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties("resolvedAt")
public record CreateIncidentResource(
        @JsonAlias("project_id") @NotNull @Positive Long projectId,
        @JsonAlias({"incidentType", "incident_type"}) @NotBlank @Size(max = 255) String type,
        @NotBlank @Size(max = 500) String description,
        @NotNull IncidentSeverity severity,
        @NotNull IncidentStatus status,
        OffsetDateTime reportedAt,
        @Positive Long reportedByUserId) {
    public com.acme.arquitech.platform.incidents.domain.model.commands.CreateIncidentCommand toCommand() {
        return new com.acme.arquitech.platform.incidents.domain.model.commands.CreateIncidentCommand(projectId, type, description, severity, status, reportedAt, reportedByUserId);
    }
}
