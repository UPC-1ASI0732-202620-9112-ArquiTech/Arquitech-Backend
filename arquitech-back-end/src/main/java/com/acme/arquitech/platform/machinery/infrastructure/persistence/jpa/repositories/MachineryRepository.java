package com.acme.arquitech.platform.machinery.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.machinery.domain.model.aggregates.Machinery;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface MachineryRepository extends JpaRepository<Machinery, Long> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("delete from Machinery a where a.projectId = :projectId")
    void deleteAllByProjectId(@org.springframework.data.repository.query.Param("projectId") Long projectId);
    List<Machinery> findByProjectIdIn(Collection<Long> projectIds);
    boolean existsByProjectIdAndSerialNumber(Long projectId, String serialNumber);
    boolean existsByProjectIdAndSerialNumberAndIdNot(Long projectId, String serialNumber, Long id);
}
