package com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories;

import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.worker.id = :workerId AND t.project.id = :projectId")
    List<Task> findByWorkerIdAndProjectId(@Param("workerId") Long workerId, @Param("projectId") Long projectId);

}