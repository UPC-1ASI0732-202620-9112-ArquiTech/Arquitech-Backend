package com.acme.arquitech.platform.iam.application.internal.authorization;

import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository users;

    public User get() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new InsufficientAuthenticationException("Authentication required");
        }
        return users.findByEmail(authentication.getName())
                .orElseThrow(() -> new InsufficientAuthenticationException("Authentication required"));
    }

    public User supervisor() {
        var user = get();
        if (user.getRole() != Role.SUPERVISOR) throw new AccessDeniedException("Supervisor required");
        return user;
    }

    public void requireSelf(Long id) {
        if (!get().getId().equals(id)) throw new AccessDeniedException("Access denied");
    }
}
