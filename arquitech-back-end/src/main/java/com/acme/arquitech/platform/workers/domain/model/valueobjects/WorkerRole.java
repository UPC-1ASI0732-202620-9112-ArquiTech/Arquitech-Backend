package com.acme.arquitech.platform.workers.domain.model.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerRole(@NotBlank @Size(min = 2, max = 50) String value) {
    public WorkerRole {
        if (value != null) {
            value = value.trim();
            if (!value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
                throw new IllegalArgumentException("Role must contain only letters and spaces");
            }
        }
    }
}