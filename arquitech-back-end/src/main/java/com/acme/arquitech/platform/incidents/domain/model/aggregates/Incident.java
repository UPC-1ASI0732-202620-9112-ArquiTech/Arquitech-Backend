package com.acme.arquitech.platform.incidents.domain.model.aggregates;

import com.acme.arquitech.platform.incidents.domain.model.valueobjects.IncidentSeverity;
import com.acme.arquitech.platform.incidents.domain.model.valueobjects.IncidentStatus;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "Incident")
public class Incident extends AuditableAbstractAggregateRoot<Incident> {

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String incidentType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @Column
    private String description;

    @Column
    private String measuresTaken;

    @Column(nullable = false)
    private Long projectId;

    public Incident(LocalDate date, String incidentType, IncidentSeverity severity, IncidentStatus status, String description, String measuresTaken, Long projectId) {
        this.date = date;
        this.incidentType = incidentType;
        this.severity = severity;
        this.status = status;
        this.description = description;
        this.measuresTaken = measuresTaken;
        this.projectId = projectId;
    }

    public Incident update(LocalDate date, String incidentType, IncidentSeverity severity, IncidentStatus status, String description, String measuresTaken) {
        this.date = date;
        this.incidentType = incidentType;
        this.severity = severity;
        this.status = status;
        this.description = description;
        this.measuresTaken = measuresTaken;
        return this;
    }
}