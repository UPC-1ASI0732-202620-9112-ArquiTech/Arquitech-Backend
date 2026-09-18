package com.acme.arquitech.platform.incidents.repositories;

import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByProjectId(Long projectId);
}