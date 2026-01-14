package cz.mp.building_diary.facade;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.dto.PhotoDto;
import cz.mp.building_diary.enums.ExportFormat;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

public interface DiaryEntryFacade {

    DiaryEntryDto createEntry(UUID projectId, DiaryEntryDto dto);

    DiaryEntryDto getEntry(UUID projectId, LocalDate date);

    Page<DiaryEntryDto> getEntries(UUID projectId, Pageable pageable);

    DiaryEntryDto updateEntry(UUID entryId, DiaryEntryDto dto);

    PhotoDto uploadPhoto(UUID diaryEntryId, MultipartFile file, String description);

    Resource getPhoto(UUID photoId);

    void deletePhoto(UUID photoId);

    byte[] export(UUID projectId, ExportFormat format);
}