package com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface WorkerRepository extends JpaRepository<Worker, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Worker a where a.project.id = :projectId")
    void deleteAllByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    List<Worker> findByProjectIdIn(Collection<Long> projectIds);
}
