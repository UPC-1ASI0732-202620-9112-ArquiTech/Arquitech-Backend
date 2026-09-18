package com.acme.arquitech.platform.workers.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateWorkerResource(
        @NotBlank @Size(min = 2, max = 100) String name,
        @NotBlank @Size(max = 50) String role,
        @NotNull LocalDate hiredDate,
        @NotNull Long projectId) {
}