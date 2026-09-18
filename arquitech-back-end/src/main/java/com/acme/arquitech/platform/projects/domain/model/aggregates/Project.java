package com.acme.arquitech.platform.projects.domain.model.aggregates;

import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Project extends AuditableAbstractAggregateRoot<Project> {
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "contractor_id", nullable = false)
    private User contractor;

    private String imageUrl;

    public Project(String name, LocalDate startDate, LocalDate endDate, BigDecimal budget, ProjectStatus status, User user, User contractor, String imageUrl) {
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.status = status;
        this.user = user;
        this.contractor = contractor;
        this.imageUrl = imageUrl;
    }

    public void setId( Long id) {
        this.id = id;
    }
}