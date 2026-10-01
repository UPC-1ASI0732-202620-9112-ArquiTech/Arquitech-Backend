package com.acme.arquitech.platform.projects.domain.model.commands;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProjectCommand(String name, String location, LocalDate startDate, LocalDate endDate, BigDecimal budget, ProjectStatus status, Integer progress, Long supervisorId, Long contractorId, String imageUrl) { }
