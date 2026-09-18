package com.acme.arquitech.platform.iam.application.internal.queryservices;

import com.acme.arquitech.platform.iam.domain.model.aggregates.User;
import com.acme.arquitech.platform.iam.domain.services.UserQueryService;
import com.acme.arquitech.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Autowired
    public UserQueryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> getById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getByName(String name) {
        return userRepository.findByName(name);
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }
}