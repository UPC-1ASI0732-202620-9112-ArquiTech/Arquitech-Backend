package com.acme.arquitech.platform.workers.interfaces.rest.resources;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerStatus;
import java.time.LocalDate;
public record WorkerResource(Long id, Long projectId, String fullName, String role, String specialty, LocalDate hireDate, WorkerStatus status) {
    public static WorkerResource from(Worker m) {
        return new WorkerResource(m.getId(), m.getProject().getId(), m.getFullName().value(), m.getRole().value(), m.getSpecialty(), m.getHireDate(), m.getStatus());
    }
}
