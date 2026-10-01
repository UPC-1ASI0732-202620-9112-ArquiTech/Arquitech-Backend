package com.acme.arquitech.platform.iam.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record UpdateUserResource(@NotBlank @Size(max = 100) String fullName, @Size(max = 30) String phone) {}
