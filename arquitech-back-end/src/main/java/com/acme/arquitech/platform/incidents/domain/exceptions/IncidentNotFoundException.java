package com.acme.arquitech.platform.incidents.domain.exceptions;

public class IncidentNotFoundException extends RuntimeException {
    public IncidentNotFoundException(Long id) {
        super("Incident with ID " + id + " not found");
    }
}