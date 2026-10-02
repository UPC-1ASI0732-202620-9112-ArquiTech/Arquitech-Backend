package com.acme.arquitech.platform.projects.domain.model.valueobjects;
@io.swagger.v3.oas.annotations.media.Schema(enumAsRef = true)
public enum ProjectStatus {
    ACTIVE, PENDING, COMPLETED, SUSPENDED,
    /** Retained only for existing database rows. */ @Deprecated PAUSED;
    public ProjectStatus canonical() { return this == PAUSED ? SUSPENDED : this; }
}
