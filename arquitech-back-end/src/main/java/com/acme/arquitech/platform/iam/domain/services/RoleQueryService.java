package com.acme.arquitech.platform.iam.domain.services;

import com.acme.arquitech.platform.iam.domain.model.valueobjects.Role;

import java.util.List;

public interface RoleQueryService {
    List<Role> getAll();
}