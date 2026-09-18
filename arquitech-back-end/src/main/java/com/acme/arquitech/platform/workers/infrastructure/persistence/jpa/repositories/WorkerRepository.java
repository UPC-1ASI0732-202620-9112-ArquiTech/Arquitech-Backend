package com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories;

import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByProjectId(Long projectId);
}