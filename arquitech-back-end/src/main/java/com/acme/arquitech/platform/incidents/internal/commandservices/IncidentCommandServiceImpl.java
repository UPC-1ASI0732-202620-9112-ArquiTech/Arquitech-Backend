package com.acme.arquitech.platform.incidents.internal.commandservices;

import com.acme.arquitech.platform.incidents.domain.exceptions.IncidentNotFoundException;
import com.acme.arquitech.platform.incidents.domain.model.aggregates.Incident;
import com.acme.arquitech.platform.incidents.domain.services.IncidentService;
import com.acme.arquitech.platform.incidents.repositories.IncidentRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

@Service
public class IncidentCommandServiceImpl implements IncidentService {

    private final IncidentRepository incidentRepository;

    public IncidentCommandServiceImpl(IncidentRepository incidentRepository) {
        this.incidentRepository = incidentRepository;
    }

    @Override
    public List<Incident> findByProjectId(Long projectId) {
        return incidentRepository.findByProjectId(projectId);
    }

    @Override
    public Optional<Incident> findById(Long id) {
        return incidentRepository.findById(id);
    }

    @Override
    public Incident create(Incident incident) {
        return incidentRepository.save(incident);
    }

    @Override
    public Incident update(Long id, Incident incident) {
        Incident existingIncident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));
        existingIncident.update(
                incident.getDate(),
                incident.getIncidentType(),
                incident.getSeverity(),
                incident.getStatus(),
                incident.getDescription(),
                incident.getMeasuresTaken()
        );
        return incidentRepository.save(existingIncident);
    }

    @Override
    public byte[] generatePdfReport(Long id) {
        Incident incident = findById(id).orElseThrow(() -> new IncidentNotFoundException(id));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.add(new Paragraph("Incident Report"));
            document.add(new Paragraph("ID: " + incident.getId()));
            document.add(new Paragraph("Date: " + incident.getDate()));
            document.add(new Paragraph("Type: " + incident.getIncidentType()));
            document.add(new Paragraph("Severity: " + incident.getSeverity()));
            document.add(new Paragraph("Status: " + incident.getStatus()));
            document.add(new Paragraph("Description: " + incident.getDescription()));
            document.add(new Paragraph("Measures Taken: " + incident.getMeasuresTaken()));
            document.add(new Paragraph("Project ID: " + incident.getProjectId()));
            document.add(new Paragraph("Created At: " + incident.getCreatedAt()));
            document.add(new Paragraph("Updated At: " + incident.getUpdatedAt()));
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }
    public List<Incident> findAll() {
        return incidentRepository.findAll();
    }
    public void delete(Long id) {
        if (!incidentRepository.existsById(id)) {
            throw new IllegalArgumentException("Incident with ID " + id + " not found");
        }
        incidentRepository.deleteById(id);
    }
}