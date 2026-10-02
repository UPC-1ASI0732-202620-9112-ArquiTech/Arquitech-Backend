package com.acme.arquitech.platform.shared.interfaces.rest.resources;

import java.time.OffsetDateTime;

public record ErrorResponse(String code, String message, OffsetDateTime timestamp, String path) {
    public static ErrorResponse of(String code, String message, String path) {
        return new ErrorResponse(code, message, OffsetDateTime.now(java.time.ZoneOffset.UTC), path);
    }
}
