package com.acme.arquitech.platform.incidents.domain.model.aggregates;
import com.acme.arquitech.platform.incidents.domain.model.valueobjects.*;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.*;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "Incident")
public class Incident extends AuditableAbstractAggregateRoot<Incident> {
    // Retained for existing NOT NULL date columns.
    @Column(nullable = false)
    private LocalDate date;
    @Column(name = "incident_type", nullable = false)
    private String type;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private IncidentSeverity severity;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private IncidentStatus status;
    @Column(length = 500)
    private String description;
    @Deprecated private String measuresTaken;
    @Column(nullable = false)
    private Long projectId;
    private Long reportedByUserId;
    private OffsetDateTime reportedAt;
    private OffsetDateTime resolvedAt;

    public Incident(Long projectId, Long reportedByUserId, String type, String description,
                    IncidentSeverity severity, IncidentStatus status, OffsetDateTime reportedAt, OffsetDateTime now) {
        this.projectId = projectId;
        this.reportedByUserId = reportedByUserId;
        this.reportedAt = reportedAt == null ? now : reportedAt;
        this.date = this.reportedAt.toLocalDate();
        update(type, description, severity, status, now);
    }

    public void update(String type, String description, IncidentSeverity severity, IncidentStatus status, OffsetDateTime now) {
        // Keep deserializing the legacy enum value so clients receive a controlled 400 response.
        if ("PENDING".equals(status.name())) throw ApiException.invalid("VALIDATION_ERROR", "Use OPEN");
        if (status == IncidentStatus.RESOLVED) {
            if (this.status != IncidentStatus.RESOLVED) resolvedAt = now;
        } else resolvedAt = null;
        this.type = type;
        this.description = description;
        this.severity = severity;
        this.status = status;
    }

    public OffsetDateTime getReportedAt() {
        return reportedAt != null ? reportedAt : date.atStartOfDay().atOffset(ZoneOffset.UTC);
    }

    public void correctReportedAt(OffsetDateTime reportedAt) {
        if (reportedAt != null) {
            this.reportedAt = reportedAt;
            this.date = reportedAt.toLocalDate();
        }
    }
}
