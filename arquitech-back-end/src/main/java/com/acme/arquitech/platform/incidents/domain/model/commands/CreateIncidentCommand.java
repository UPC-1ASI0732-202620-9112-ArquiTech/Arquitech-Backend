package com.acme.arquitech.platform.incidents.domain.model.commands;
import com.acme.arquitech.platform.incidents.domain.model.valueobjects.*;
import java.time.OffsetDateTime;

public record CreateIncidentCommand(Long projectId, String type, String description, IncidentSeverity severity, IncidentStatus status, OffsetDateTime reportedAt, Long reportedByUserId) { }
