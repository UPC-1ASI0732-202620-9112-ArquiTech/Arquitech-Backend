package com.acme.arquitech.platform.workers.domain.model.aggregates;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "workers")
@Getter
@NoArgsConstructor
public class Worker extends AuditableAbstractAggregateRoot<Worker> {
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private WorkerName fullName;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "role", nullable = false))
    private WorkerRole role;
    @Column(name = "hired_date", nullable = false)
    private LocalDate hireDate;
    @ManyToOne @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    private String specialty;
    @Enumerated(EnumType.STRING)
    private WorkerStatus status;

    public Worker(Project project, String fullName, String role, String specialty, LocalDate hireDate, WorkerStatus status) {
        this.project = project;
        update(fullName, role, specialty, hireDate, status);
    }
    public void update(String fullName, String role, String specialty, LocalDate hireDate, WorkerStatus status) {
        this.fullName = new WorkerName(fullName);
        this.role = new WorkerRole(role);
        this.specialty = specialty;
        this.hireDate = hireDate;
        this.status = status;
    }
    public WorkerStatus getStatus() { return status == null ? WorkerStatus.ACTIVE : status; }
}
