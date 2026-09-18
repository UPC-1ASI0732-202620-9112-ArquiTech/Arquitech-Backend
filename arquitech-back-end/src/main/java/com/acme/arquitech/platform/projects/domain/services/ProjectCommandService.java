package com.acme.arquitech.platform.projects.domain.services;

import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface ProjectCommandService {
    Project create(String name, LocalDate startDate, LocalDate endDate, BigDecimal budget, ProjectStatus status, Long userId, Long contractorId, String imageUrl);
}