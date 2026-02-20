package cz.mp.construction_site_diary.facade.impl;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.facade.DiaryEntryFacade;
import cz.mp.construction_site_diary.service.DiaryEntryService;
import cz.mp.construction_site_diary.service.DiaryExportService;
import cz.mp.construction_site_diary.enums.ExportFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiaryEntryFacadeImpl implements DiaryEntryFacade {

    private final DiaryEntryService diaryEntryService;
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
    public byte[] export(UUID projectId, ExportFormat format) {
        return diaryExportService.export(projectId, format);
    }
}
