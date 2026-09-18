package com.acme.arquitech.platform.iam.interfaces.rest.resources;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;

public record UserResource(Long id, String name, String email, Role role, String profilePicture) {
}
