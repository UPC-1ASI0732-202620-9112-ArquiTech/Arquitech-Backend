package com.acme.arquitech.platform.materials.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MaterialTransactionResource(
        Long id,
        @JsonProperty("project_id")
        Long projectId,
        @JsonProperty("name")
        String name,
        @JsonProperty("quantity")
        Integer quantity,
        String provider,
        String date,
        @JsonProperty("entry_type")
        String entryType,
       @JsonProperty("exit_type")
        String exitType,
        @JsonProperty("exit_date")
        String exitDate
) {
}