package com.acme.arquitech.platform.attendance.interfaces.rest;
import com.acme.arquitech.platform.attendance.domain.services.*;
import com.acme.arquitech.platform.attendance.interfaces.rest.resources.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
@RestController @RequestMapping(value = "/api/v1/attendance", produces = "application/json")
@Tag(name = "Attendance") @RequiredArgsConstructor
public class AttendanceController {
    private final AttendanceCommandService commands;
    private final AttendanceQueryService queries;
    @GetMapping @Operation(summary = "List attendance of an accessible project, optionally for a date")
    public List<AttendanceResource> list(@RequestParam @Positive Long projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return queries.list(projectId, date).stream().map(AttendanceResource::from).toList();
    }
    @PostMapping @Operation(summary = "Record worker attendance as supervisor")
    public ResponseEntity<AttendanceResource> create(@Valid @RequestBody CreateAttendanceResource resource) {
        return ResponseEntity.status(HttpStatus.CREATED).body(AttendanceResource.from(commands.create(resource.toCommand())));
    }
    @PutMapping("/{id}") @Operation(summary = "Correct attendance as supervisor")
    public AttendanceResource update(@PathVariable Long id, @Valid @RequestBody UpdateAttendanceResource resource) {
        return AttendanceResource.from(commands.update(id, resource.toCommand()));
    }
    @DeleteMapping("/{id}") @Operation(summary = "Delete attendance as supervisor")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        commands.delete(id);
        return ResponseEntity.noContent().build();
    }
}
