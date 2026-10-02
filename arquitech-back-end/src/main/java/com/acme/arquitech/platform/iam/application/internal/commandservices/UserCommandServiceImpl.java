package com.acme.arquitech.platform.iam.application.internal.commandservices;

import com.acme.arquitech.platform.iam.application.internal.authorization.CurrentUserService;
import com.acme.arquitech.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.acme.arquitech.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.model.commands.*;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.iam.domain.services.UserCommandService;
import com.acme.arquitech.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.acme.arquitech.platform.shared.domain.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final CurrentUserService currentUser;

    @Override
    @Transactional(readOnly = true)
    public ImmutablePair<User, String> handle(SignInCommand command) {
        var user = userRepository.findByEmail(command.email().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!hashingService.matches(command.password(), user.getPassword()))
            throw new BadCredentialsException("Invalid credentials");
        return ImmutablePair.of(user, tokenService.generateToken(user.getEmail()));
    }

    @Override
    public User handle(SignUpCommand command) {
        if (command.role() != Role.SUPERVISOR && command.role() != Role.CONTRACTOR)
            throw ApiException.invalid("VALIDATION_ERROR", "Role must be SUPERVISOR or CONTRACTOR");
        if (command.password().getBytes(StandardCharsets.UTF_8).length > 72)
            throw ApiException.invalid("VALIDATION_ERROR", "Password must not exceed 72 UTF-8 bytes");
        String email = command.email().trim();
        if (userRepository.existsByEmail(email))
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "Email already exists");
        var user = new User(command.fullName().trim(), email, hashingService.encode(command.password()),
                command.role(), command.profilePicture(), command.phone());
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException ex) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "Email already exists");
        }
    }

    @Override
    public User updateProfile(Long id, String fullName, String phone) {
        currentUser.requireSelf(id);
        var user = currentUser.get();
        user.updateProfile(fullName.trim(), phone);
        return userRepository.save(user);
    }
}
