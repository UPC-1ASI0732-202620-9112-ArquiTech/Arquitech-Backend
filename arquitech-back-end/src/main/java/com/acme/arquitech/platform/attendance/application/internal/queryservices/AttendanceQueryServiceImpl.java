package com.acme.arquitech.platform.attendance.application.internal.queryservices;
import com.acme.arquitech.platform.attendance.domain.model.aggregates.Attendance;
import com.acme.arquitech.platform.attendance.domain.services.AttendanceQueryService;
import com.acme.arquitech.platform.attendance.infrastructure.persistence.jpa.repositories.AttendanceRepository;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class AttendanceQueryServiceImpl implements AttendanceQueryService {
    private final AttendanceRepository repository;
    private final ProjectAccessService access;
    public List<Attendance> list(Long projectId, LocalDate date) {
        access.requireRead(projectId);
        return date == null ? repository.findByProjectIdOrderByAttendanceDateDescIdDesc(projectId)
                : repository.findByProjectIdAndAttendanceDateOrderByIdDesc(projectId, date);
    }
}
