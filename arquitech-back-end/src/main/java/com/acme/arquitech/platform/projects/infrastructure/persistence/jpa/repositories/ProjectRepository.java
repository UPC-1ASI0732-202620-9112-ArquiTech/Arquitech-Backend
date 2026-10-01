package com.acme.arquitech.platform.projects.infrastructure.persistence.jpa.repositories;
import com.acme.arquitech.platform.projects.domain.model.aggregates.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findBySupervisorId(Long id);
    List<Project> findByContractorId(Long id);
    @Query("select p.id from Project p where p.supervisor.id = :id")
    List<Long> findIdsBySupervisorId(@Param("id") Long id);
    @Query("select p.id from Project p where p.contractor.id = :id")
    List<Long> findIdsByContractorId(@Param("id") Long id);
}
