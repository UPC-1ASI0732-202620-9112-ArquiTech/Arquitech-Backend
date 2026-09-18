package com.acme.arquitech.platform.iam.interfaces.rest;


import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.services.UserCommandService;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.SignInResource;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.SignUpResource;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.UserResource;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/authentication", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;

    public AuthenticationController(UserCommandService userCommandService) {
        this.userCommandService = userCommandService;
    }

    /**
     * Handles the sign-in request.
     * @param signInResource the sign-in request body.
     * @return the authenticated user resource.
     */
    @PostMapping("/sign-in")
    @Operation(summary = "Sign-in", description = "Sign-in with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User authenticated successfully."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials.")})
    public ResponseEntity<AuthenticatedUserResource> signIn(@RequestBody SignInResource signInResource) {
        var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
        try {
            var authenticatedUser = userCommandService.handle(signInCommand);
            if (authenticatedUser.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            ImmutablePair<User, String> result = authenticatedUser.get();
            var resource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(result.getLeft(), result.getRight());
            return ResponseEntity.ok(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Handles the sign-up request.
     * @param signUpResource the sign-up request body.
     * @return the created user resource.
     */
    @PostMapping("/sign-up")
    @Operation(summary = "Sign-up", description = "Sign-up with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        try {
            var user = userCommandService.handle(signUpCommand);
            if (user.isEmpty()) return ResponseEntity.badRequest().build();
            var resource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
            return ResponseEntity.status(HttpStatus.CREATED).body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}