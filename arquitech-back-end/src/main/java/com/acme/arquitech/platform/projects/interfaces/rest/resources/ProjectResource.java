package com.acme.arquitech.platform.projects.interfaces.rest.resources;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public record ProjectResource(Long id, String name, String location, LocalDate startDate, LocalDate endDate,
        BigDecimal budget, ProjectStatus status, Integer progress, Long supervisorId, Long contractorId,
        String supervisorName, String contractorName, Date createdAt, String imageUrl) {
    public static ProjectResource from(Project p) {
        return new ProjectResource(p.getId(), p.getName(), p.getLocation(), p.getStartDate(), p.getEndDate(),
                p.getBudget(), p.getStatus().canonical(), p.getProgress(), p.getSupervisor().getId(), p.getContractor().getId(),
                p.getSupervisor().getName(), p.getContractor().getName(), p.getCreatedAt(), p.getImageUrl());
    }
}
