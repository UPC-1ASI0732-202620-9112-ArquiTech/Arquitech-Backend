package com.acme.arquitech.platform.projects.interfaces.rest.resources;

import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProjectResource(Long id, String name, LocalDate startDate, LocalDate endDate, BigDecimal budget, ProjectStatus status, Long userId, Long contractorId, String imageUrl) {
}