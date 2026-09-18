package com.acme.arquitech.platform.machinery.infrastructure.persistence.jpa.repositories;

import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    Optional<Machinery> findByLicensePlate(String licensePlate);
}