package com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.tasks.domain.model.aggregates.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByProjectIdIn(Collection<Long> projectIds);
    boolean existsByWorkerId(Long workerId);
}
