package com.acme.arquitech.platform.workers.domain.model.valueobjects;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
@Embeddable
public record WorkerRole(@NotBlank @Size(max = 50) String value) {
    public WorkerRole {
        if (value != null) value = value.trim();
    }
}
