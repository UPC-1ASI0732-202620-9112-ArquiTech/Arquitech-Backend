package com.acme.arquitech.platform.machinery.interfaces.rest.resources;
import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record UpdateMachineryResource(
        @JsonAlias({"project_id", "id_project"}) @Positive Long projectId,
        @NotBlank @Size(max = 255) String name,
        @JsonAlias({"licensePlate", "license_plate"}) @NotBlank @Size(max = 255) String serialNumber,
        @JsonAlias({"registerDate", "register_date"}) @NotNull LocalDate registeredAt,
        @NotNull MachineryStatus status,
        @Size(max = 255) String description) {
    public com.acme.arquitech.platform.machinery.domain.model.commands.UpdateMachineryCommand toCommand() {
        return new com.acme.arquitech.platform.machinery.domain.model.commands.UpdateMachineryCommand(projectId, name, serialNumber, registeredAt, status, description);
    }
}
