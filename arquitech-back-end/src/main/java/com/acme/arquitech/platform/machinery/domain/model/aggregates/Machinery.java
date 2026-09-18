package com.acme.arquitech.platform.machinery.domain.model.aggregates;

import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "machineries")
@Getter
@NoArgsConstructor
public class Machinery extends AuditableAbstractAggregateRoot<Machinery> {

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Setter
    @Column(nullable = false)
    private String name;
    @Setter
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;
    @Setter
    @Column(name = "register_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date registerDate;
    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MachineryStatus status;

    public Machinery(Long projectId, String name, String licensePlate, Date registerDate, MachineryStatus status) {
        this.projectId = projectId;
        this.name = name;
        this.licensePlate = licensePlate;
        this.registerDate = registerDate;
        this.status = status;
    }

    public Machinery update(String name, String licensePlate, Date registerDate, MachineryStatus status) {
        this.name = name;
        this.licensePlate = licensePlate;
        this.registerDate = registerDate;
        this.status = status;
        return this;
    }
}