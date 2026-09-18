package com.acme.arquitech.platform.incidents.interfaces.rest;

import com.acme.arquitech.platform.incidents.internal.commandservices.IncidentCommandServiceImpl;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.rest.resources.CreateIncidentResource;
import com.acme.arquitech.platform.incidents.rest.resources.IncidentResource;
import com.acme.arquitech.platform.incidents.rest.resources.UpdateIncidentResource;
import com.acme.arquitech.platform.shared.interfaces.rest.resources.MessageResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/incidents")
@Tag(name = "Incidents", description = "Incident Management Endpoints")
public class IncidentController {

    private final IncidentCommandServiceImpl incidentService;

    public IncidentController(IncidentCommandServiceImpl incidentService) {
        this.incidentService = incidentService;
    }

    @Operation(summary = "Get incidents by project ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of incidents retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No incidents found for given project ID")
    })
    @GetMapping("/project/{projectId}")
    public List<IncidentResource> getIncidentsByProjectId(@PathVariable Long projectId) {
        return incidentService.findByProjectId(projectId).stream()
                .map(incident -> new IncidentResource(
                        incident.getId(),
                        incident.getDate(),
                        incident.getIncidentType(),
                        incident.getSeverity(),
                        incident.getStatus(),
                        incident.getDescription(),
                        incident.getMeasuresTaken(),
                        incident.getProjectId(),
                        incident.getCreatedAt(),
                        incident.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Operation(summary = "Get incident by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    @GetMapping("/{id}")
    public IncidentResource getIncidentById(@PathVariable Long id) {
        Incident incident = incidentService.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found"));
        return new IncidentResource(
                incident.getId(),
                incident.getDate(),
                incident.getIncidentType(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getDescription(),
                incident.getMeasuresTaken(),
                incident.getProjectId(),
                incident.getCreatedAt(),
                incident.getUpdatedAt()
        );
    }

    @Operation(summary = "Create a new incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid incident data")
    })
    @PostMapping
    public ResponseEntity<MessageResource> createIncident(@RequestBody CreateIncidentResource resource) {
        Incident incident = new Incident(
                resource.date(),
                resource.incidentType(),
                resource.severity(),
                resource.status(),
                resource.description(),
                resource.measuresTaken(),
                resource.projectId()
        );
        incidentService.create(incident);
        return ResponseEntity.ok(new MessageResource("Incident created successfully"));
    }

    @Operation(summary = "Update an existing incident")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incident updated successfully"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MessageResource> updateIncident(@PathVariable Long id, @RequestBody UpdateIncidentResource resource) {
        Incident incident = new Incident(
                resource.date(),
                resource.incidentType(),
                resource.severity(),
                resource.status(),
                resource.description(),
                resource.measuresTaken(),
                null // projectId not updated
        );
        incidentService.update(id, incident);
        return ResponseEntity.ok(new MessageResource("Incident updated successfully"));
    }

    @Operation(summary = "Download incident report as PDF")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PDF report downloaded successfully"),
            @ApiResponse(responseCode = "404", description = "Incident not found")
    })
    @GetMapping(value = "/{id}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadIncidentReport(@PathVariable Long id) {
        byte[] pdfContent = incidentService.generatePdfReport(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "incident-report-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdfContent);
    }

    @Operation(summary = "Get all incidents")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All incidents retrieved successfully")
    })
    @GetMapping
    public List<IncidentResource> getAllIncidents() {
        return incidentService.findAll().stream()
                .map(incident -> new IncidentResource(
                        incident.getId(),
                        incident.getDate(),
                        incident.getIncidentType(),
                        incident.getSeverity(),
                        incident.getStatus(),
                        incident.getDescription(),
                        incident.getMeasuresTaken(),
                        incident.getProjectId(),
                        incident.getCreatedAt(),
                        incident.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an incident by ID")
    public ResponseEntity<MessageResource> deleteIncident(@PathVariable Long id) {
        incidentService.delete(id);
        return ResponseEntity.ok(new MessageResource("Incident deleted successfully"));
    }
}