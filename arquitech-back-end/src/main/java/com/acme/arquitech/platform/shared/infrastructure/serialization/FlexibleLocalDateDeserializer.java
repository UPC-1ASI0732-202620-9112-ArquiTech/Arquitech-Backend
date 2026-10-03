package com.acme.arquitech.platform.shared.infrastructure.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

/** Accepts a calendar date or an ISO 8601 date-time while keeping the domain type as LocalDate. */
public class FlexibleLocalDateDeserializer extends JsonDeserializer<LocalDate> {
    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        String value = parser.getValueAsString();
        if (value == null || value.isBlank()) {
            return (LocalDate) context.handleUnexpectedToken(LocalDate.class, parser);
        }

        String normalized = value.trim();
        try {
            return LocalDate.parse(normalized);
        } catch (DateTimeParseException ignored) {
            // Angular sends the selected date as an ISO date-time with an offset.
        }

        try {
            return OffsetDateTime.parse(normalized).toLocalDate();
        } catch (DateTimeParseException exception) {
            throw context.weirdStringException(value, LocalDate.class,
                    "Expected YYYY-MM-DD or an ISO 8601 date-time with offset");
        }
    }
}
