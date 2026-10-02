package com.acme.arquitech.platform.incidents.domain.exceptions;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import org.springframework.http.HttpStatus;
public class IncidentNotFoundException extends ApiException {
    public IncidentNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "INCIDENT_NOT_FOUND", "Incident with id " + id + " was not found");
    }
}
