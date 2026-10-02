package com.acme.arquitech.platform.incidents.repositories;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    List<Incident> findByProjectIdIn(Collection<Long> projectIds);
}
