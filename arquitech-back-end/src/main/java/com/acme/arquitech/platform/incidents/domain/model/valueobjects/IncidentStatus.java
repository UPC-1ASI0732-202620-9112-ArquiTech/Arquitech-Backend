package com.acme.arquitech.platform.incidents.domain.model.valueobjects;
@io.swagger.v3.oas.annotations.media.Schema(enumAsRef = true)
public enum IncidentStatus {
    OPEN, IN_REVIEW, RESOLVED, @Deprecated PENDING;
    public IncidentStatus canonical() { return this == PENDING ? OPEN : this; }
}
