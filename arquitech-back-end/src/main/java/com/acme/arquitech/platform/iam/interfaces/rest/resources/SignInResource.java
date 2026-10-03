package com.acme.arquitech.platform.iam.interfaces.rest.resources;
import jakarta.validation.constraints.*;
public record SignInResource(@NotBlank @Email @Size(max = 254) String email,
                             @NotBlank @Size(max = 72) String password) {
    @Override
    public String toString() {
        return "SignInResource[email=" + email + ", password=[REDACTED]]";
    }
}
