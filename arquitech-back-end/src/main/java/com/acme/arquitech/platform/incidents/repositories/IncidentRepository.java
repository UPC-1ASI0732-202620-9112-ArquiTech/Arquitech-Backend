package com.acme.arquitech.platform.incidents.repositories;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Incident a where a.projectId = :projectId")
    void deleteAllByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    List<Incident> findByProjectIdIn(Collection<Long> projectIds);
}
