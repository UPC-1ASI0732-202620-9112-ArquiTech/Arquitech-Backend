package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
public record MaterialUsageResource(@NotNull @Positive Integer quantity,
                                    @NotNull OffsetDateTime occurredAt, @Size(max = 2000) String note) {
    public com.acme.arquitech.platform.materials.domain.model.commands.MaterialUsageCommand toCommand() {
        return new com.acme.arquitech.platform.materials.domain.model.commands.MaterialUsageCommand(quantity, occurredAt, note);
    }
}
