package com.acme.arquitech.platform.projects.domain.model.aggregates;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Project extends AuditableAbstractAggregateRoot<Project> {
    @Column(nullable = false)
    private String name;
    private String location;
    @Column(nullable = false)
    private LocalDate startDate;
    @Column(nullable = false)
    private LocalDate endDate;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal budget;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ProjectStatus status;
    private Integer progress;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User supervisor;
    @ManyToOne
    @JoinColumn(name = "contractor_id", nullable = false)
    private User contractor;
    private String imageUrl;

    public Project(String name, String location, LocalDate startDate, LocalDate endDate, BigDecimal budget,
                   ProjectStatus status, Integer progress, User supervisor, User contractor, String imageUrl) {
        if (endDate.isBefore(startDate)) throw ApiException.invalid("VALIDATION_ERROR", "endDate must be on or after startDate");
        if (budget.signum() < 0 || progress < 0 || progress > 100)
            throw ApiException.invalid("VALIDATION_ERROR", "Invalid budget or progress");
        this.name = name;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.budget = budget;
        this.status = status;
        this.progress = progress;
        this.supervisor = supervisor;
        this.contractor = contractor;
        this.imageUrl = imageUrl;
    }
    public Integer getProgress() { return progress == null ? 0 : progress; }
}
