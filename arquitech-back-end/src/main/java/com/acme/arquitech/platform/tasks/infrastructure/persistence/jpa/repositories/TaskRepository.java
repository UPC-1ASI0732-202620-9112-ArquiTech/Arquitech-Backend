package com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TaskRepository extends JpaRepository<Task, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Task a where a.project.id = :projectId")
    void deleteAllByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    List<Task> findByProjectIdIn(Collection<Long> projectIds);
    boolean existsByWorkerId(Long workerId);
}
