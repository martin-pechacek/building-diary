package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.PhotoDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface PhotoService {

    PhotoDto upload(UUID diaryEntryId, MultipartFile file, String description);

    Resource getFile(UUID photoId);

    void delete(UUID photoId);
}