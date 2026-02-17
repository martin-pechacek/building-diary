package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.exception.ProjectNotFoundException;
import cz.mp.building_diary.repository.DiaryEntryRepository;
import cz.mp.building_diary.repository.ProjectRepository;
import cz.mp.building_diary.service.ProjectService;
import cz.mp.building_diary.strategy.DiaryExportStrategy;
import cz.mp.building_diary.enums.ExportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryExportServiceImplTest {

    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

    @Mock
    private DiaryEntryRepository diaryEntryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private DiaryExportStrategy csvExportStrategy;

    @Mock
    private DiaryExportStrategy pdfExportStrategy;

    private DiaryExportServiceImpl diaryExportService;

    private Project project;
    private DiaryEntry diaryEntry;

    @BeforeEach
    void setUp() {
        Map<ExportFormat, DiaryExportStrategy> exportStrategies = Map.of(
                ExportFormat.CSV, csvExportStrategy,
                ExportFormat.PDF, pdfExportStrategy
        );

        diaryExportService = new DiaryExportServiceImpl(
                diaryEntryRepository,
                projectRepository,
                projectService,
                exportStrategies
        );

        project = new Project();
        project.setId(PROJECT_ID);
        project.setName("Test Project");

        diaryEntry = new DiaryEntry();
        diaryEntry.setId(UUID.randomUUID());
        diaryEntry.setDate(LocalDate.of(2024, 1, 15));
        diaryEntry.setWeatherCondition("Sunny");
        diaryEntry.setTemperature(22.5);
        diaryEntry.setSummary("Work completed on foundation");
        diaryEntry.setProject(project);
    }

    @Nested
    class ExportToCsv {

        @Test
        void shouldExportDiaryEntriesToCsv() {
            byte[] expectedData = "csv content".getBytes();
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryRepository.findByProjectIdOrderByDateAsc(PROJECT_ID))
                    .thenReturn(List.of(diaryEntry));
            when(csvExportStrategy.export(project, List.of(diaryEntry))).thenReturn(expectedData);

            byte[] result = diaryExportService.export(PROJECT_ID, ExportFormat.CSV);

            assertThat(result).isEqualTo(expectedData);
            verify(projectService).hasAccess(PROJECT_ID);
            verify(csvExportStrategy).export(project, List.of(diaryEntry));
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryExportService.export(PROJECT_ID, ExportFormat.CSV))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryRepository, never()).findByProjectIdOrderByDateAsc(PROJECT_ID);
        }
    }

    @Nested
    class ExportToPdf {

        @Test
        void shouldExportDiaryEntriesToPdf() {
            byte[] expectedData = "%PDF content".getBytes();
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryRepository.findByProjectIdOrderByDateAsc(PROJECT_ID))
                    .thenReturn(List.of(diaryEntry));
            when(pdfExportStrategy.export(project, List.of(diaryEntry))).thenReturn(expectedData);

            byte[] result = diaryExportService.export(PROJECT_ID, ExportFormat.PDF);

            assertThat(result).isEqualTo(expectedData);
            verify(projectService).hasAccess(PROJECT_ID);
            verify(pdfExportStrategy).export(project, List.of(diaryEntry));
        }

        @Test
        void shouldExportEmptyPdfWhenNoEntries() {
            byte[] expectedData = "%PDF empty".getBytes();
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryRepository.findByProjectIdOrderByDateAsc(PROJECT_ID))
                    .thenReturn(Collections.emptyList());
            when(pdfExportStrategy.export(project, Collections.emptyList())).thenReturn(expectedData);

            byte[] result = diaryExportService.export(PROJECT_ID, ExportFormat.PDF);

            assertThat(result).isEqualTo(expectedData);
            verify(projectService).hasAccess(PROJECT_ID);
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryExportService.export(PROJECT_ID, ExportFormat.PDF))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryRepository, never()).findByProjectIdOrderByDateAsc(PROJECT_ID);
        }
    }
}