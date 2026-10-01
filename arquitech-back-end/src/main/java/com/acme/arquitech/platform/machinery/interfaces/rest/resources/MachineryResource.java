package com.acme.arquitech.platform.machinery.interfaces.rest.resources;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import java.time.LocalDate;
public record MachineryResource(Long id, Long projectId, String name, String serialNumber, MachineryStatus status, LocalDate registeredAt, String description) {
    public static MachineryResource from(Machinery m) {
        return new MachineryResource(m.getId(), m.getProjectId(), m.getName(), m.getSerialNumber(), m.getStatus().canonical(), m.getRegisteredAt(), m.getDescription());
    }
}
