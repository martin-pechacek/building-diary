package cz.mp.building_diary.facade.impl;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.dto.PhotoDto;
import cz.mp.building_diary.facade.DiaryEntryFacade;
import cz.mp.building_diary.service.DiaryEntryService;
import cz.mp.building_diary.service.DiaryExportService;
import cz.mp.building_diary.service.PhotoService;
import cz.mp.building_diary.enums.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryEntryFacadeImpl implements DiaryEntryFacade {

    private final DiaryEntryService diaryEntryService;
    private final PhotoService photoService;
    private final DiaryExportService diaryExportService;

    @Override
    public DiaryEntryDto createEntry(UUID projectId, DiaryEntryDto dto) {
        return diaryEntryService.create(projectId, dto);
    }

    @Override
    public DiaryEntryDto getEntry(UUID projectId, LocalDate date) {
        return diaryEntryService.getByProjectIdAndDate(projectId, date);
    }

    @Override
    public Page<DiaryEntryDto> getEntries(UUID projectId, Pageable pageable) {
        return diaryEntryService.getAllByProjectId(projectId, pageable);
    }

    @Override
    public DiaryEntryDto updateEntry(UUID entryId, DiaryEntryDto dto) {
        return diaryEntryService.update(entryId, dto);
    }

    @Override
    public PhotoDto uploadPhoto(UUID diaryEntryId, MultipartFile file, String description) {
        return photoService.upload(diaryEntryId, file, description);
    }

    @Override
    public Resource getPhoto(UUID photoId) {
        return photoService.getFile(photoId);
    }

    @Override
    public void deletePhoto(UUID photoId) {
        photoService.delete(photoId);
    }

    @Override
    public byte[] export(UUID projectId, ExportFormat format) {
        return diaryExportService.export(projectId, format);
    }
}
