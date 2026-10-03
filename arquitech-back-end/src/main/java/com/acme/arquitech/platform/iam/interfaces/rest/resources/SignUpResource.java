package com.acme.arquitech.platform.iam.interfaces.rest.resources;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
public record SignUpResource(
        @JsonAlias("name") @NotBlank @Size(max = 100) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Role role,
        @Size(max = 255) String profilePicture,
        @Size(max = 30) String phone) {
    @Override
    public String toString() {
        return "SignUpResource[fullName=" + fullName + ", email=" + email
                + ", password=[REDACTED], role=" + role + ", profilePicture=" + profilePicture
                + ", phone=" + phone + "]";
    }
}
