package com.acme.arquitech.platform.iam.interfaces.acl;
import com.acme.arquitech.platform.iam.domain.model.commands.SignUpCommand;
import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;
import com.acme.arquitech.platform.iam.domain.services.UserCommandService;
import com.acme.arquitech.platform.iam.domain.services.UserQueryService;

import java.util.List;

public class IamContextFacade {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    public IamContextFacade(UserCommandService userCommandService, UserQueryService userQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
    }

    public Long createUser(String username, String password) {
        var command = new SignUpCommand(username, username, password, Role.USER, "");
        var result = userCommandService.handle(command);
        return result.map(u -> u.getId()).orElse(0L);
    }

    public Long createUser(String username, String password, List<String> roleNames) {
        var role = roleNames != null && !roleNames.isEmpty() ? Role.valueOf(roleNames.get(0)) : Role.USER;
        var command = new SignUpCommand(username, username, password, role, "");
        var result = userCommandService.handle(command);
        return result.map(u -> u.getId()).orElse(0L);
    }

    public Long fetchUserIdByUsername(String username) {
        return userQueryService.getByName(username)
                .map(u -> u.getId())
                .orElse(0L);
    }

    public String fetchUsernameByUserId(Long userId) {
        return userQueryService.getById(userId)
                .map(u -> u.getName())
                .orElse("");
    }
}