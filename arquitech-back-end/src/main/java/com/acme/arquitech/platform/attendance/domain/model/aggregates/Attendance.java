package com.acme.arquitech.platform.attendance.domain.model.aggregates;
import com.acme.arquitech.platform.attendance.domain.model.valueobjects.AttendanceStatus;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import com.acme.arquitech.platform.workers.domain.model.aggregates.Worker;
import com.acme.arquitech.platform.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "attendance_records", uniqueConstraints = @UniqueConstraint(name = "uk_attendance_worker_date", columnNames = {"worker_id", "attendance_date"}),
        indexes = @Index(name = "idx_attendance_project_date", columnList = "project_id, attendance_date"))
@Getter @NoArgsConstructor
public class Attendance extends AuditableAbstractAggregateRoot<Attendance> {
    @ManyToOne(optional = false) @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    @ManyToOne(optional = false) @JoinColumn(name = "worker_id", nullable = false)
    private Worker worker;
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AttendanceStatus status;
    private OffsetDateTime checkInAt;
    private OffsetDateTime checkOutAt;
    @Column(length = 1000) private String notes;
    @ManyToOne(optional = false) @JoinColumn(name = "registered_by_user_id", nullable = false)
    private User registeredBy;

    public Attendance(Project project, Worker worker, LocalDate date, AttendanceStatus status,
                      OffsetDateTime checkInAt, OffsetDateTime checkOutAt, String notes, User registeredBy) {
        this.project = project;
        this.registeredBy = registeredBy;
        update(worker, date, status, checkInAt, checkOutAt, notes);
    }
    public void update(Worker worker, LocalDate date, AttendanceStatus status,
                       OffsetDateTime checkInAt, OffsetDateTime checkOutAt, String notes) {
        if (!project.getId().equals(worker.getProject().getId()))
            throw ApiException.invalid("VALIDATION_ERROR", "Worker must belong to the project");
        if (date.isBefore(worker.getHireDate()))
            throw ApiException.invalid("VALIDATION_ERROR", "Attendance cannot precede the hire date");
        if (checkOutAt != null && (checkInAt == null || checkOutAt.isBefore(checkInAt)))
            throw ApiException.invalid("VALIDATION_ERROR", "Check-out requires check-in and cannot precede it");
        if ((status == AttendanceStatus.ABSENT || status == AttendanceStatus.EXCUSED)
                && (checkInAt != null || checkOutAt != null))
            throw ApiException.invalid("VALIDATION_ERROR", "Absent or excused attendance cannot have working times");
        this.worker = worker;
        this.attendanceDate = date;
        this.status = status;
        this.checkInAt = checkInAt == null ? null : checkInAt.withOffsetSameInstant(ZoneOffset.UTC);
        this.checkOutAt = checkOutAt == null ? null : checkOutAt.withOffsetSameInstant(ZoneOffset.UTC);
        this.notes = notes == null ? null : notes.trim();
    }
}
