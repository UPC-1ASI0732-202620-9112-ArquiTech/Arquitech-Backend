package com.acme.arquitech.platform.materials.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateMaterialResource(
        String name,
        Integer quantity,
        @JsonProperty("unit_price")
        BigDecimal unitPrice,
        String unit,
        String provider,
        @JsonProperty("provider_ruc")
        String providerRuc,
        String date,
        @JsonProperty("receipt_number")
        String receiptNumber,
        @JsonProperty("payment_method")
        String paymentMethod,
        @JsonProperty("entry_type")
        String entryType,
        @JsonProperty("entry_date")
        String exitType,
        @JsonProperty("exit_date")
        String exitDate
) {
}