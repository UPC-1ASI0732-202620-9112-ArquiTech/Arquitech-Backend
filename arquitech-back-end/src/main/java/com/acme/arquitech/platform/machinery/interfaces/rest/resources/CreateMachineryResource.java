package com.acme.arquitech.platform.machinery.interfaces.rest.resources;
import com.acme.arquitech.platform.machinery.domain.model.valueobjects.MachineryStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
public record CreateMachineryResource(
        @JsonAlias({"project_id", "id_project"}) @NotNull @Positive Long projectId,
        @NotBlank @Size(max = 255) String name,
        @JsonAlias({"licensePlate", "license_plate"}) @NotBlank @Size(max = 255) String serialNumber,
        @JsonAlias({"registerDate", "register_date"}) @NotNull @PastOrPresent LocalDate registeredAt,
        @NotNull MachineryStatus status,
        @Size(max = 255) String description) {
    public com.acme.arquitech.platform.machinery.domain.model.commands.CreateMachineryCommand toCommand() {
        return new com.acme.arquitech.platform.machinery.domain.model.commands.CreateMachineryCommand(projectId, name, serialNumber, registeredAt, status, description);
    }
}
