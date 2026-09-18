package com.acme.arquitech.platform.incidents.rest.resources;

import com.acme.arquitech.platform.incidents.domain.model.valueobjects.IncidentSeverity;
import com.acme.arquitech.platform.incidents.domain.model.valueobjects.IncidentStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.Date;

public record IncidentResource(
        @JsonProperty("id") Long id,
        @JsonProperty("date") LocalDate date,
        @JsonProperty("incident_type") String incidentType,
        @JsonProperty("severity") IncidentSeverity severity,
        @JsonProperty("status") IncidentStatus status,
        @JsonProperty("description") String description,
        @JsonProperty("measures_taken") String measuresTaken,
        @JsonProperty("project_id") Long projectId,
        @JsonProperty("created_at") Date createdAt,
        @JsonProperty("updated_at") Date updatedAt
) {
}