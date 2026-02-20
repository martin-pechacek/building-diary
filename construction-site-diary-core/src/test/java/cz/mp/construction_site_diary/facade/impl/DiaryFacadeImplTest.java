package cz.mp.construction_site_diary.facade.impl;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.service.DiaryEntryService;
import cz.mp.construction_site_diary.service.DiaryExportService;
import cz.mp.construction_site_diary.enums.ExportFormat;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryFacadeImplTest {

    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    @Mock
    private DiaryEntryService diaryEntryService;

    @Mock
    private DiaryExportService diaryExportService;

    @InjectMocks
    private DiaryEntryFacadeImpl diaryEntryFacade;

    @Nested
    class Entries {

        @Test
        void shouldCreateEntry() {
            DiaryEntryDto inputDto = createEntryDto(null);
            DiaryEntryDto savedDto = createEntryDto(ENTRY_ID);

            when(diaryEntryService.create(PROJECT_ID, inputDto)).thenReturn(savedDto);

            DiaryEntryDto result = diaryEntryFacade.createEntry(PROJECT_ID, inputDto);

            assertThat(result.id()).isEqualTo(ENTRY_ID);
            verify(diaryEntryService).create(PROJECT_ID, inputDto);
        }

        @Test
        void shouldGetEntryByDate() {
            LocalDate date = LocalDate.of(2024, 1, 15);
            DiaryEntryDto entryDto = createEntryDto(ENTRY_ID);

            when(diaryEntryService.getByProjectIdAndDate(PROJECT_ID, date)).thenReturn(entryDto);

            DiaryEntryDto result = diaryEntryFacade.getEntry(PROJECT_ID, date);

            assertThat(result.id()).isEqualTo(ENTRY_ID);
            verify(diaryEntryService).getByProjectIdAndDate(PROJECT_ID, date);
        }

        @Test
        void shouldGetEntriesPaginated() {
            Pageable pageable = PageRequest.of(0, 10);
            DiaryEntryDto entryDto = createEntryDto(ENTRY_ID);
            Page<DiaryEntryDto> page = new PageImpl<>(List.of(entryDto));

            when(diaryEntryService.getAllByProjectId(PROJECT_ID, pageable)).thenReturn(page);

            Page<DiaryEntryDto> result = diaryEntryFacade.getEntries(PROJECT_ID, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(diaryEntryService).getAllByProjectId(PROJECT_ID, pageable);
        }

        @Test
        void shouldUpdateEntry() {
            DiaryEntryDto inputDto = createEntryDto(ENTRY_ID);
            DiaryEntryDto updatedDto = createEntryDto(ENTRY_ID);

            when(diaryEntryService.update(ENTRY_ID, inputDto)).thenReturn(updatedDto);

            DiaryEntryDto result = diaryEntryFacade.updateEntry(ENTRY_ID, inputDto);

            assertThat(result.id()).isEqualTo(ENTRY_ID);
            verify(diaryEntryService).update(ENTRY_ID, inputDto);
        }
    }

    @Nested
    class Export {

        @Test
        void shouldExportToCsv() {
            byte[] exportedData = "date,summary\n2024-01-15,Test".getBytes();

            when(diaryExportService.export(PROJECT_ID, ExportFormat.CSV)).thenReturn(exportedData);

            byte[] result = diaryEntryFacade.export(PROJECT_ID, ExportFormat.CSV);

            assertThat(result).isEqualTo(exportedData);
            verify(diaryExportService).export(PROJECT_ID, ExportFormat.CSV);
        }

        @Test
        void shouldExportToPdf() {
            byte[] exportedData = new byte[]{0x25, 0x50, 0x44, 0x46}; // PDF magic bytes

            when(diaryExportService.export(PROJECT_ID, ExportFormat.PDF)).thenReturn(exportedData);

            byte[] result = diaryEntryFacade.export(PROJECT_ID, ExportFormat.PDF);

            assertThat(result).isEqualTo(exportedData);
            verify(diaryExportService).export(PROJECT_ID, ExportFormat.PDF);
        }
    }

    private DiaryEntryDto createEntryDto(UUID id) {
        return new DiaryEntryDto(
                id,
                PROJECT_ID,
                LocalDate.of(2024, 1, 15),
                "Test summary",
                "Sunny",
                22.5,
                List.of(),
                List.of(),
                UUID.randomUUID(),
                Instant.now(),
                Instant.now()
        );
    }
}