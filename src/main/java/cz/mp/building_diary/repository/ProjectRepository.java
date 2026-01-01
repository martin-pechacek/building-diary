package cz.mp.building_diary.repository;

import cz.mp.building_diary.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    List<Project> findByCreatedById(UUID userId);

    @Query("SELECT p FROM Project p WHERE p.id = :id AND p.archived = true")
    Optional<Project> findArchivedById(@Param("id") UUID id);

    @Query("SELECT p FROM Project p WHERE p.createdBy.id = :userId AND p.archived = true")
    List<Project> findArchivedByCreatedById(@Param("userId") UUID userId);
}