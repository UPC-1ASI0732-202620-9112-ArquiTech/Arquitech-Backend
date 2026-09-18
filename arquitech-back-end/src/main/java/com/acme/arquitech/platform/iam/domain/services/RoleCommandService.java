package com.acme.arquitech.platform.iam.domain.services;

import com.acme.arquitech.platform.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
