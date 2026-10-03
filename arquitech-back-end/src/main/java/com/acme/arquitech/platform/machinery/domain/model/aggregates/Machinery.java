package com.acme.arquitech.platform.machinery.domain.model.aggregates;
import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "machineries", uniqueConstraints = @UniqueConstraint(
        name = "uk_machineries_project_serial",
        columnNames = {"project_id", "license_plate"}))
@Getter
@NoArgsConstructor
public class Machinery extends AuditableAbstractAggregateRoot<Machinery> {
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    @Column(nullable = false)
    private String name;
    @Column(name = "license_plate", nullable = false)
    private String serialNumber;
    @Column(name = "register_date", nullable = false)
    private LocalDate registeredAt;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private MachineryStatus status;
    private String description;

    public Machinery(Long projectId, String name, String serialNumber, LocalDate registeredAt,
                     MachineryStatus status, String description) {
        this.projectId = projectId;
        update(name, serialNumber, registeredAt, status, description);
    }
    public void update(String name, String serialNumber, LocalDate registeredAt, MachineryStatus status, String description) {
        if (status != status.canonical()) throw ApiException.invalid("VALIDATION_ERROR", "Use a current machinery status");
        this.name = name;
        this.serialNumber = serialNumber.trim();
        this.registeredAt = registeredAt;
        this.status = status;
        this.description = description;
    }
}
