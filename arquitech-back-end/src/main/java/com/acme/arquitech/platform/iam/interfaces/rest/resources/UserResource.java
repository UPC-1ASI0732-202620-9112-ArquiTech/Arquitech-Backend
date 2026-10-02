package com.acme.arquitech.platform.iam.interfaces.rest.resources;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import java.time.OffsetDateTime;
public record UserResource(Long id, String fullName, String email, Role role, String phone,
                           OffsetDateTime createdAt, String profilePicture) {}
