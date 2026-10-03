package com.acme.arquitech.platform.materials.interfaces.rest.resources;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateMaterialResource(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 255) String unit,
        @JsonAlias("minimum_stock") @NotNull @DecimalMin("0") @Digits(integer = 15, fraction = 4) BigDecimal minimumStock,
        @JsonAlias("unit_price") @NotNull @DecimalMin("0") @Digits(integer = 17, fraction = 2) BigDecimal unitPrice,
        @NotBlank @Size(max = 255) String provider,
        @JsonAlias("provider_ruc") @NotBlank @Pattern(regexp = "^(10|15|17|20)[0-9]{9}$") String providerRuc) {
    public com.acme.arquitech.platform.materials.domain.model.commands.UpdateMaterialCommand toCommand() {
        return new com.acme.arquitech.platform.materials.domain.model.commands.UpdateMaterialCommand(name, unit, minimumStock, unitPrice, provider, providerRuc);
    }
}
