package com.acme.arquitech.platform.attendance.application.internal.commandservices;
import com.acme.arquitech.platform.attendance.domain.model.aggregates.Attendance;
import com.acme.arquitech.platform.attendance.domain.model.commands.*;
import com.acme.arquitech.platform.attendance.domain.services.AttendanceCommandService;
import com.acme.arquitech.platform.attendance.infrastructure.persistence.jpa.repositories.AttendanceRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.workers.infrastructure.persistence.jpa.repositories.WorkerRepository;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.workers.domain.model.valueobjects.WorkerStatus;
import com.acme.arquitech.platform.workers.domain.exceptions.WorkerNotFoundException;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional @PreAuthorize("hasAuthority('SUPERVISOR')")
public class AttendanceCommandServiceImpl implements AttendanceCommandService {
    private final AttendanceRepository repository;
    private final WorkerRepository workers;
    private final ProjectAccessService access;
    private final CurrentUserService currentUser;
    public Attendance create(CreateAttendanceCommand c) {
        var project = access.requireWrite(c.projectId());
        var worker = worker(c.workerId(), c.projectId(), null);
        if (repository.existsByWorkerIdAndAttendanceDate(c.workerId(), c.attendanceDate())) throw duplicate();
        return repository.saveAndFlush(new Attendance(project, worker, c.attendanceDate(), c.status(),
                c.checkInAt(), c.checkOutAt(), c.notes(), currentUser.supervisor()));
    }
    public Attendance update(Long id, UpdateAttendanceCommand c) {
        var item = writable(id);
        var worker = worker(c.workerId(), item.getProject().getId(), item.getWorker().getId());
        if (repository.existsByWorkerIdAndAttendanceDateAndIdNot(c.workerId(), c.attendanceDate(), id)) throw duplicate();
        item.update(worker, c.attendanceDate(), c.status(), c.checkInAt(), c.checkOutAt(), c.notes());
        return repository.saveAndFlush(item);
    }
    public void delete(Long id) { repository.delete(writable(id)); }
    private Attendance writable(Long id) {
        var item = repository.findById(id).orElseThrow(() -> ApiException.notFound("ATTENDANCE_NOT_FOUND", "Attendance not found"));
        access.requireWrite(item.getProject().getId());
        return item;
    }
    private Worker worker(Long id, Long projectId, Long existingWorkerId) {
        var worker = workers.findById(id).orElseThrow(() -> new WorkerNotFoundException(id));
        if (!worker.getProject().getId().equals(projectId)) throw ApiException.invalid("VALIDATION_ERROR", "Worker must belong to the project");
        if (worker.getStatus() == WorkerStatus.INACTIVE && !id.equals(existingWorkerId))
            throw ApiException.invalid("VALIDATION_ERROR", "Inactive workers cannot receive new attendance records");
        return worker;
    }
    private ApiException duplicate() { return ApiException.conflict("DUPLICATE_ATTENDANCE", "Attendance already exists for this worker and date"); }
}
