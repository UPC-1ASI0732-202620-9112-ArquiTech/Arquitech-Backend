package com.acme.arquitech.platform.attendance.domain.services;
import com.acme.arquitech.platform.attendance.domain.model.aggregates.Attendance;
import com.acme.arquitech.platform.attendance.domain.model.commands.*;
public interface AttendanceCommandService {
    Attendance create(CreateAttendanceCommand command);
    Attendance update(Long id, UpdateAttendanceCommand command);
    void delete(Long id);
}
