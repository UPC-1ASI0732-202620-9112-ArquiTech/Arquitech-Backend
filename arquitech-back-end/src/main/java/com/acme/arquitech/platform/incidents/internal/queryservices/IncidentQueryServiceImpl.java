package com.acme.arquitech.platform.incidents.internal.queryservices;

import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.domain.services.IncidentService;
import com.acme.arquitech.platform.incidents.repositories.IncidentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class IncidentQueryServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentQueryServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public List<Incident> findByProjectId(Long projectId) {
        return incidentRepository.findByProjectId(projectId);
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return incidentRepository.findById(id);
    }

    @Override
    public Incident create(Incident incident) {
        throw new UnsupportedOperationException("Create operation is handled by command service");
    }

    @Override
    public Incident update(Long id, Incident incident) {
        throw new UnsupportedOperationException("Update operation is handled by command service");
    }

    @Override
    public byte[] generatePdfReport(Long id) {
        throw new UnsupportedOperationException("PDF generation is handled by command service");
    }
}