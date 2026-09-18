package com.acme.arquitech.platform.materials.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CreateMaterialResource(
        @JsonProperty("project_id")
        Long projectId,
        @JsonProperty("name")
        String name,

        @JsonProperty("quantity")
        Integer quantity,

        @JsonProperty("unit_price")
        BigDecimal unitPrice,

        @JsonProperty("unit")
        String unit,

        @JsonProperty("provider")
        String provider,

        @JsonProperty("provider_ruc")
        String providerRuc,

        @JsonProperty("date")
        String date,

        @JsonProperty("receipt_number")
        String receiptNumber,

        @JsonProperty("payment_method")
        String paymentMethod,

        @JsonProperty("entry_type")
        String entryType
) {
}