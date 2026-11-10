package com.acme.arquitech.platform.materials.interfaces.rest.resources;

import com.acme.arquitech.platform.materials.domain.model.valueobjects.MovementType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

/**
 * A durable client-generated request id identifies one offline movement.
 * Replays with identical payloads return the already-created movement.
 */
public record OfflineMovementResource(
        @NotBlank @Size(min = 8, max = 128) @Pattern(regexp = "[A-Za-z0-9:_-]+") String clientRequestId,
        @NotNull MovementType type,
        @NotNull @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 15, fraction = 4) BigDecimal quantity,
        @Size(max = 255) String supplier,
        @NotNull OffsetDateTime occurredAt,
        @Size(max = 2000) String note) {
}
