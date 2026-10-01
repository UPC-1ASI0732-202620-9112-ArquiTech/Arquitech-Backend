package com.acme.arquitech.platform.machinery.domain.service;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import com.acme.arquitech.platform.machinery.domain.model.commands.*;
public interface MachineryService {
    Machinery create(CreateMachineryCommand resource);
    Machinery update(Long id, UpdateMachineryCommand resource);
    void delete(Long id);
}
