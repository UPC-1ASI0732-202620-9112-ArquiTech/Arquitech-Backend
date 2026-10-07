package com.acme.arquitech.platform.attendance.domain.model.commands;
import com.acme.arquitech.platform.attendance.domain.model.valueobjects.AttendanceStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;
public record UpdateAttendanceCommand(Long workerId, LocalDate attendanceDate,
        AttendanceStatus status, OffsetDateTime checkInAt, OffsetDateTime checkOutAt, String notes) {}
