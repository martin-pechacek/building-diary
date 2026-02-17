package cz.mp.building_diary.repository;

import cz.mp.building_diary.entity.DiaryEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DiaryEntryRepository extends JpaRepository<DiaryEntry, UUID> {

    Page<DiaryEntry> findByProjectIdOrderByDateDesc(UUID projectId, Pageable pageable);

    Optional<DiaryEntry> findByProjectIdAndDate(UUID projectId, LocalDate date);

    List<DiaryEntry> findByProjectIdAndDateBetweenOrderByDateAsc(UUID projectId, LocalDate startDate, LocalDate endDate);

    List<DiaryEntry> findByProjectIdOrderByDateAsc(UUID projectId);

    long countByProjectId(UUID projectId);
}