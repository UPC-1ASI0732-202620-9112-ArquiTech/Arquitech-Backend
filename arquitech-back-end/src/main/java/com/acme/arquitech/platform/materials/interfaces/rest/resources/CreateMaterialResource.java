package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMaterialResource(
        @JsonAlias("project_id") @NotNull @Positive Long projectId,
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String unit,
        @NotNull @PositiveOrZero Integer quantity,
        @JsonAlias("minimum_stock") @NotNull @PositiveOrZero Integer minimumStock,
        @JsonAlias("unit_price") @NotNull @DecimalMin("0") @Digits(integer = 17, fraction = 2) BigDecimal unitPrice,
        @NotBlank @Size(max = 255) String provider,
        @JsonAlias("provider_ruc") @NotBlank @Pattern(regexp = "^(10|15|17|20)[0-9]{9}$") String providerRuc,
        @NotNull LocalDate date) {
    public com.acme.arquitech.platform.materials.domain.model.commands.CreateMaterialCommand toCommand() {
        return new com.acme.arquitech.platform.materials.domain.model.commands.CreateMaterialCommand(projectId, name, unit, quantity, minimumStock, unitPrice, provider, providerRuc, date);
    }
}
