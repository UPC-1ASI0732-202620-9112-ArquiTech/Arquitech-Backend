package com.acme.arquitech.platform.incidents.domain.services;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.domain.model.commands.*;
public interface IncidentService {
    Incident create(CreateIncidentCommand resource);
    Incident update(Long id, UpdateIncidentCommand resource);
    void delete(Long id);
}
