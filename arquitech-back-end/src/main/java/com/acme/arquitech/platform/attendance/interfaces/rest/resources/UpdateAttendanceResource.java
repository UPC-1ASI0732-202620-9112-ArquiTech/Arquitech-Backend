package com.acme.arquitech.platform.attendance.interfaces.rest.resources;
import com.acme.arquitech.platform.attendance.domain.model.valueobjects.AttendanceStatus;
import com.acme.arquitech.platform.attendance.domain.model.commands.UpdateAttendanceCommand;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
public record UpdateAttendanceResource(@NotNull @Positive Long workerId,
        @NotNull LocalDate attendanceDate, @NotNull AttendanceStatus status,
        OffsetDateTime checkInAt, OffsetDateTime checkOutAt, @Size(max = 1000) String notes) {
    public UpdateAttendanceCommand toCommand() {
        return new UpdateAttendanceCommand(workerId, attendanceDate, status, checkInAt, checkOutAt, notes);
    }
}
