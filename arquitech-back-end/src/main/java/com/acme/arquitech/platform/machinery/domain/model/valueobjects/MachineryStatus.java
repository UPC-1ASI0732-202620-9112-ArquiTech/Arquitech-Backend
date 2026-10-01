package com.acme.arquitech.platform.machinery.domain.model.valueobjects;
@io.swagger.v3.oas.annotations.media.Schema(enumAsRef = true)
public enum MachineryStatus {
    OPERATIONAL, MAINTENANCE, OUT_OF_SERVICE,
    @Deprecated UNDER_MAINTENANCE, @Deprecated AVAILABLE;
    public MachineryStatus canonical() {
        return this == UNDER_MAINTENANCE ? MAINTENANCE : this == AVAILABLE ? OPERATIONAL : this;
    }
}
