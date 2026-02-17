package cz.mp.photos_service.repository;

import cz.mp.photos_service.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PhotoRepository extends JpaRepository<Photo, UUID> {

    List<Photo> findByDiaryEntryId(UUID diaryEntryId);

    List<Photo> findByDiaryEntryIdIn(List<UUID> diaryEntryIds);

    void deleteAllByDiaryEntryId(UUID diaryEntryId);
}
