package com.acme.arquitech.platform.workers.interfaces.rest.resources;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record WorkerResource(
        Long id,
        String name,
        String role,
        @JsonProperty("hired_date") LocalDate hiredDate,
        @JsonProperty("project_id") Long projectId) {
}