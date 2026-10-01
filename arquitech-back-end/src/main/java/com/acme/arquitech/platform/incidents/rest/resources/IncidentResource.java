package com.acme.arquitech.platform.incidents.rest.resources;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.domain.model.valueobjects.*;
import java.time.OffsetDateTime;
public record IncidentResource(Long id, Long projectId, Long reportedByUserId, String type, String description,
        IncidentSeverity severity, IncidentStatus status, OffsetDateTime reportedAt, OffsetDateTime resolvedAt) {
    public static IncidentResource from(Incident i) {
        return new IncidentResource(i.getId(), i.getProjectId(), i.getReportedByUserId(), i.getType(),
                i.getDescription(), i.getSeverity(), i.getStatus().canonical(), i.getReportedAt(), i.getResolvedAt());
    }
}
