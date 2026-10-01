package com.acme.arquitech.platform.tasks.domain.model.valueobjects;
@io.swagger.v3.oas.annotations.media.Schema(enumAsRef = true)
public enum TaskStatus {
    PENDING, IN_PROGRESS, COMPLETED, @Deprecated DONE;
    public TaskStatus canonical() { return this == DONE ? COMPLETED : this; }
}
