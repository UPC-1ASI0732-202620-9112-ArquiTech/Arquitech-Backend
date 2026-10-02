package com.acme.arquitech.platform.incidents.internal.queryservices;
import com.acme.arquitech.platform.projects.application.authorization.ProjectAccessService;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.domain.exceptions.IncidentNotFoundException;
import com.acme.arquitech.platform.incidents.repositories.IncidentRepository;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentQueryServiceImpl {
    private final IncidentRepository repository;
    private final ProjectAccessService access;
    public Incident findById(Long id) {
        var incident = repository.findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
        access.requireRead(incident.getProjectId());
        return incident;
    }
    public List<Incident> findAll(Long projectId) {
        var scope = access.scope(projectId);
        return scope.isEmpty() ? List.of() : repository.findByProjectIdIn(scope);
    }
    public byte[] generatePdfReport(Long id) {
        var incident = findById(id);
        var output = new ByteArrayOutputStream();
        try (var document = new Document(new PdfDocument(new PdfWriter(output)))) {
            document.add(new Paragraph("ArquiTech - Incident Report"));
            document.add(new Paragraph("ID: " + incident.getId()));
            document.add(new Paragraph("Reported At: " + incident.getReportedAt()));
            document.add(new Paragraph("Type: " + incident.getType()));
            document.add(new Paragraph("Severity: " + incident.getSeverity()));
            document.add(new Paragraph("Status: " + incident.getStatus().canonical()));
            document.add(new Paragraph("Description: " + incident.getDescription()));
            document.add(new Paragraph("Project ID: " + incident.getProjectId()));
            document.add(new Paragraph("Resolved At: " + incident.getResolvedAt()));
        }
        return output.toByteArray();
    }
}
