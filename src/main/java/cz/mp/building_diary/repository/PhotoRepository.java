package cz.mp.building_diary.repository;

import cz.mp.building_diary.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findByDiaryEntryId(UUID diaryEntryId);
}