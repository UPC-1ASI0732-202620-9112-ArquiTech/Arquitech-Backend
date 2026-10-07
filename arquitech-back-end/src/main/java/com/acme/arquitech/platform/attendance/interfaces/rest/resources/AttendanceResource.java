package com.acme.arquitech.platform.attendance.interfaces.rest.resources;
import com.acme.arquitech.platform.attendance.domain.model.aggregates.Attendance;
import com.acme.arquitech.platform.attendance.domain.model.valueobjects.AttendanceStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Date;
public record AttendanceResource(Long id, Long projectId, Long workerId, String workerName,
        LocalDate attendanceDate, AttendanceStatus status, OffsetDateTime checkInAt, OffsetDateTime checkOutAt,
        String notes, Long registeredByUserId, Date createdAt, Date updatedAt) {
    public static AttendanceResource from(Attendance a) {
        return new AttendanceResource(a.getId(), a.getProject().getId(), a.getWorker().getId(), a.getWorker().getFullName().value(),
                a.getAttendanceDate(), a.getStatus(), a.getCheckInAt(), a.getCheckOutAt(), a.getNotes(),
                a.getRegisteredBy().getId(), a.getCreatedAt(), a.getUpdatedAt());
    }
}
