package com.acme.arquitech.platform.iam.interfaces.rest;
import com.acme.arquitech.platform.iam.domain.services.*;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.*;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/users", produces = "application/json")
@Tag(name = "Users")
@RequiredArgsConstructor
public class UsersController {
    private final UserQueryService queryService;
    private final UserCommandService commandService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    @Operation(summary = "List users for project assignment")
    public List<UserResource> getAll() {
        return queryService.getAll().stream().map(UserResourceFromEntityAssembler::toResourceFromEntity).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read a user profile")
    public UserResource getById(@PathVariable Long id) {
        return UserResourceFromEntityAssembler.toResourceFromEntity(queryService.getById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update your own name and phone")
    public UserResource update(@PathVariable Long id, @Valid @RequestBody UpdateUserResource resource) {
        return UserResourceFromEntityAssembler.toResourceFromEntity(commandService.updateProfile(id, resource.fullName(), resource.phone()));
    }
}
