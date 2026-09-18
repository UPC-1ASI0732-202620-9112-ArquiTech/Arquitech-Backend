package com.acme.arquitech.platform.workers.domain.model.valueobjects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerName(@NotBlank @Size(min = 2, max = 100) String value) {
    public WorkerName {
        if (value != null) {
            value = value.trim();
            if (!value.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
                throw new IllegalArgumentException("Name must contain only letters and spaces");
            }
        }
    }
}