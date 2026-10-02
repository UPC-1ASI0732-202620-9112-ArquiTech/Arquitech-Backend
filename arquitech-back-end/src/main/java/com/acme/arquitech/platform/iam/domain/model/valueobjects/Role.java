package com.acme.arquitech.platform.iam.domain.model.valueobjects;

/**
 * Roles
 * <p>
 *     This enum represents the roles in the system.
 * </p>
 */
@io.swagger.v3.oas.annotations.media.Schema(enumAsRef = true)
public enum Role {
    SUPERVISOR,
    CONTRACTOR,
    /** Only for reading legacy accounts; registration rejects this role. */
    @Deprecated USER
}
