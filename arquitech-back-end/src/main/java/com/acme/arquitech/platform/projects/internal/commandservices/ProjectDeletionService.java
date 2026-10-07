package com.acme.arquitech.platform.projects.internal.commandservices;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories.ProjectRepository;
import com.acme.arquitech.platform.attendance.infrastructure.persistence.jpa.repositories.AttendanceRepository;
import com.acme.arquitech.platform.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import com.acme.arquitech.platform.materials.infrastructure.persistence.jpa.repositories.*;
import com.acme.arquitech.platform.machinery.infrastructure.persistence.jpa.repositories.MachineryRepository;
import com.acme.arquitech.platform.incidents.repositories.IncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
@Service @RequiredArgsConstructor @Transactional @PreAuthorize("hasAuthority('SUPERVISOR')")
public class ProjectDeletionService {
    private final ProjectAccessService access;
    private final ProjectRepository projects;
    private final AttendanceRepository attendance;
    private final TaskRepository tasks;
    private final MaterialMovementRepository movements;
    private final WorkerRepository workers;
    private final MaterialRepository materials;
    private final MachineryRepository machinery;
    private final IncidentRepository incidents;
    public void delete(Long id) {
        var project = access.requireWrite(id);
        // Children first: the whole operation rolls back if any constraint fails.
        attendance.deleteAllByProjectId(id);
        tasks.deleteAllByProjectId(id);
        movements.deleteAllByProjectId(id);
        workers.deleteAllByProjectId(id);
        materials.deleteAllByProjectId(id);
        machinery.deleteAllByProjectId(id);
        incidents.deleteAllByProjectId(id);
        projects.delete(project);
        projects.flush();
    }
}
