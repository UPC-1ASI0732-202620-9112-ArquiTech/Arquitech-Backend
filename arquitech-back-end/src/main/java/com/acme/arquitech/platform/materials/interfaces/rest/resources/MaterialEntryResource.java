package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;
import java.math.BigDecimal;
public record MaterialEntryResource(@NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 15, fraction = 4) BigDecimal quantity, @Size(max = 255) String supplier,
                                    @NotNull OffsetDateTime occurredAt, @Size(max = 2000) String note) {
    public com.acme.arquitech.platform.materials.domain.model.commands.MaterialEntryCommand toCommand() {
        return new com.acme.arquitech.platform.materials.domain.model.commands.MaterialEntryCommand(quantity, supplier, occurredAt, note);
    }
}
