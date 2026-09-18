package com.acme.arquitech.platform.incidents.domain.services;

import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;

import java.util.List;
import java.util.Optional;

public interface IncidentService {
    List<Incident> findByProjectId(Long projectId);
    Optional<Incident> findById(Long id);
    Incident create(Incident incident);
    Incident update(Long id, Incident incident);
    byte[] generatePdfReport(Long id);
}