package com.acme.arquitech.platform.projects.interfaces.rest.resources;
import com.acme.arquitech.platform.projects.domain.model.valueobjects.ProjectStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record CreateProjectResource(
        @NotBlank String name,
        @JsonProperty("start_date")
        @NotNull @FutureOrPresent LocalDate startDate,
        @JsonProperty("end_date")
        @NotNull @Future LocalDate endDate,
        @JsonProperty("budget")
        @NotNull @DecimalMin("0.00") BigDecimal budget,
        @JsonProperty("status")
        @NotNull ProjectStatus status,
        @JsonProperty("user_id")
        @NotNull @Positive Long userId,
        @JsonProperty("contractor_id")
        @NotNull @Positive Long contractorId,
        @JsonProperty("image_url")
        String imageUrl) {
}