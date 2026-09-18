package com.acme.arquitech.platform.machinery.domain.service;

import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;

import java.util.List;
import java.util.Optional;

public interface MachineryService {
    Machinery create(Machinery machinery);
    Optional<Machinery> getById(Long id);
    List<Machinery> getAll();
}