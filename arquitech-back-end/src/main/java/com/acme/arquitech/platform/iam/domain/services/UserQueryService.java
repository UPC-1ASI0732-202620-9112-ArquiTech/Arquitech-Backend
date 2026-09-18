package com.acme.arquitech.platform.iam.domain.services;

import com.acme.arquitech.platform.iam.domain.model.aggregates.User;

import java.util.List;
import java.util.Optional;

public interface UserQueryService {
    Optional<User> getById(Long id);
    Optional<User> getByName(String name);
    List<User> getAll();
}