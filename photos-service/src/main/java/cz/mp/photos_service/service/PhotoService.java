package cz.mp.photos_service.service;

import cz.mp.photos_service.dto.PhotoDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface PhotoService {

    PhotoDto upload(UUID diaryEntryId, UUID projectId, MultipartFile file, String description);

    Resource getFile(UUID photoId);

    void delete(UUID photoId);

    List<PhotoDto> getByDiaryEntryId(UUID diaryEntryId);

    void deleteByDiaryEntryId(UUID diaryEntryId);
}
