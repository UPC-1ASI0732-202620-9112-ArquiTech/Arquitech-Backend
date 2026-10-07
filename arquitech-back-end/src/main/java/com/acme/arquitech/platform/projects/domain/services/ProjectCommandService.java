package com.acme.arquitech.platform.projects.domain.services;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.projects.domain.model.commands.CreateProjectCommand;
public interface ProjectCommandService { Project create(CreateProjectCommand resource); void delete(Long id); }
