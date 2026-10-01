package com.acme.arquitech.platform.iam.interfaces.rest;
import com.acme.arquitech.platform.iam.domain.services.UserCommandService;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.*;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = "application/json")
@Tag(name = "Authentication")
@SecurityRequirements
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserCommandService userCommandService;

    @PostMapping("/sign-in")
    @Operation(summary = "Sign in using email and password")
    public AuthenticatedUserResource signIn(@Valid @RequestBody SignInResource resource) {
        var result = userCommandService.handle(SignInCommandFromResourceAssembler.toCommandFromResource(resource));
        return AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(result.getLeft(), result.getRight());
    }

    @PostMapping("/sign-up")
    @Operation(summary = "Register a supervisor or contractor")
    public ResponseEntity<UserResource> signUp(@Valid @RequestBody SignUpResource resource) {
        var user = userCommandService.handle(SignUpCommandFromResourceAssembler.toCommandFromResource(resource));
        return ResponseEntity.status(201).body(UserResourceFromEntityAssembler.toResourceFromEntity(user));
    }
}
