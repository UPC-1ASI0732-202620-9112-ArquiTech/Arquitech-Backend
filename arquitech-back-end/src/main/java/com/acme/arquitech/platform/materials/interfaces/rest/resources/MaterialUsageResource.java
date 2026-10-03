package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
public record MaterialUsageResource(@NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 15, fraction = 4) BigDecimal quantity,
                                    @NotNull OffsetDateTime occurredAt, @Size(max = 2000) String note) {
    public com.acme.arquitech.platform.materials.domain.model.commands.MaterialUsageCommand toCommand() {
        return new com.acme.arquitech.platform.materials.domain.model.commands.MaterialUsageCommand(quantity, occurredAt, note);
    }
}
