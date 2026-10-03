package com.acme.arquitech.platform.iam.interfaces.rest.resources;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
public record AuthenticatedUserResource(Long id, String fullName, String email, Role role, String token) {
    @Override
    public String toString() {
        return "AuthenticatedUserResource[id=" + id + ", fullName=" + fullName + ", email=" + email
                + ", role=" + role + ", token=[REDACTED]]";
    }
}
