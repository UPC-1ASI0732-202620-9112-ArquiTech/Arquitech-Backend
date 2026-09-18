package com.acme.arquitech.platform.workers.domain.model.aggregates;

import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerName;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "workers")
@Getter
@Setter
@NoArgsConstructor
public class Worker extends AuditableAbstractAggregateRoot<Worker> {
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    })
    private WorkerName name;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "role", nullable = false))
    })
    private WorkerRole role;

    @Column(name = "hired_date", nullable = false)
    private LocalDate hiredDate;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    public Worker(WorkerName name, WorkerRole role, LocalDate hiredDate, Project project) {
        this.name = name;
        this.role = role;
        this.hiredDate = hiredDate;
        this.project = project;
    }

    public void setId(Long id) {
        this.id=id;
    }
}