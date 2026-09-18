package com.acme.arquitech.platform.iam.interfaces.rest.resources;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;

public record SignUpResource(String name, String email, String password, Role role, String profilePicture) {
}