package com.acme.arquitech.platform.attendance.domain.services;
import com.acme.arquitech.platform.attendance.domain.model.aggregates.Attendance;
import java.time.LocalDate;
import java.util.List;
public interface AttendanceQueryService { List<Attendance> list(Long projectId, LocalDate date); }
