package com.acme.arquitech.platform.tasks.interfaces.rest.resources;

import com.acme.arquitech.platform.tasks.domain.model.valueobjects.TaskStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public record CreateTaskResource(
        @JsonProperty("id_project")Long projectId,
        @JsonProperty("id_worker")Long workerId,
        @JsonProperty("description") String description,
        @JsonProperty("start_date") LocalDate startDate,
        @JsonProperty("due_date") LocalDate dueDate,
        @JsonProperty("status") TaskStatus status) {
}