package cz.mp.construction_site_diary.repository;

import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
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

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Project p " +
            "WHERE p.id = :projectId AND (p.createdBy.id = :userId OR p.constructionManager.id = :userId)")
    boolean hasAccess(@Param("projectId") UUID projectId, @Param("userId") UUID userId);

    @Query("SELECT p.status FROM Project p WHERE p.id = :projectId")
    ProjectStatus findStatusById(@Param("projectId") UUID projectId);
}