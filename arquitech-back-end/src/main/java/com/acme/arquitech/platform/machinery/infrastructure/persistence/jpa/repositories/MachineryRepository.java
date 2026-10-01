package com.acme.arquitech.platform.machinery.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    List<Machinery> findByProjectIdIn(Collection<Long> projectIds);
    boolean existsBySerialNumber(String serialNumber);
    boolean existsBySerialNumberAndIdNot(String serialNumber, Long id);
}
