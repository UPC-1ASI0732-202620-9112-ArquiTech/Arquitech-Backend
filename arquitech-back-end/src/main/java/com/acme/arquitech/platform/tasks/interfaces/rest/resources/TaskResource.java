package com.acme.arquitech.platform.tasks.interfaces.rest.resources;

import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record TaskResource(
        Long id,
        @JsonProperty("id_project") Long projectId,
        @JsonProperty("id_worker") Long workerId,
        String description,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("due_date") LocalDate dueDate,
        TaskStatus status) {
}