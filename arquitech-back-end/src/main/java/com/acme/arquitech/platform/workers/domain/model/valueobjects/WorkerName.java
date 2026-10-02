package com.acme.arquitech.platform.workers.domain.model.valueobjects;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.*;
@Embeddable
public record WorkerName(@NotBlank @Size(max = 100) String value) {
    public WorkerName {
        if (value != null) value = value.trim();
    }
}
