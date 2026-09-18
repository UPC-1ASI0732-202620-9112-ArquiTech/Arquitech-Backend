package com.acme.arquitech.platform.iam.interfaces.rest;
import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.model.commands.SignInCommand;
import com.acme.arquitech.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.arquitech.platform.iam.domain.services.UserCommandService;
import com.acme.arquitech.platform.iam.domain.services.UserQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private final UserQueryService userQueryService;

    @Autowired
    public UsersController( UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        return userQueryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<User> getAll() {
        return userQueryService.getAll();
    }
}
