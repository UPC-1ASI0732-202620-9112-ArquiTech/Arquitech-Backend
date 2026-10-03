package com.acme.arquitech.platform.iam.interfaces.rest;
import com.acme.arquitech.platform.iam.domain.services.*;
import com.acme.arquitech.platform.iam.interfaces.rest.resources.*;
import com.acme.arquitech.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

}
