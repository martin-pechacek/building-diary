package cz.mp.construction_site_diary.facade;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.enums.ExportFormat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface DiaryEntryFacade {

    DiaryEntryDto createEntry(UUID projectId, DiaryEntryDto dto);

    DiaryEntryDto getEntry(UUID projectId, LocalDate date);

    Page<DiaryEntryDto> getEntries(UUID projectId, Pageable pageable);

    DiaryEntryDto updateEntry(UUID entryId, DiaryEntryDto dto);

    byte[] export(UUID projectId, ExportFormat format);
}
