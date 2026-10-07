package com.acme.arquitech.platform.attendance.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.attendance.domain.model.aggregates.Attendance;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByProjectIdOrderByAttendanceDateDescIdDesc(Long projectId);
    List<Attendance> findByProjectIdAndAttendanceDateOrderByIdDesc(Long projectId, LocalDate date);
    boolean existsByWorkerIdAndAttendanceDate(Long workerId, LocalDate date);
    boolean existsByWorkerIdAndAttendanceDateAndIdNot(Long workerId, LocalDate date, Long id);
    boolean existsByWorkerId(Long workerId);
    @Modifying @Query("delete from Attendance a where a.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") Long projectId);
}
