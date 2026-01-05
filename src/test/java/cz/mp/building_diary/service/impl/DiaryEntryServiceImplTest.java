package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.dto.MaterialUsageDto;
import cz.mp.building_diary.dto.WorkforceEntryDto;
import cz.mp.building_diary.entity.DiaryEntry;
import cz.mp.building_diary.entity.MaterialUsage;
import cz.mp.building_diary.entity.Project;
import cz.mp.building_diary.entity.WorkforceEntry;
import cz.mp.building_diary.exception.DiaryEntryAlreadyExistsException;
import cz.mp.building_diary.exception.ProjectNotFoundException;
import cz.mp.building_diary.exception.ProjectStateException;
import cz.mp.building_diary.mapper.DiaryEntryMapper;
import cz.mp.building_diary.mapper.ListMapper;
import cz.mp.building_diary.mapper.MaterialUsageMapper;
import cz.mp.building_diary.mapper.WorkforceEntryMapper;
import cz.mp.building_diary.repository.DiaryEntryRepository;
import cz.mp.building_diary.repository.ProjectRepository;
import cz.mp.building_diary.service.ProjectService;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DiaryEntryServiceImplTest {

    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final LocalDate ENTRY_DATE = LocalDate.of(2024, 1, 15);

    @Mock
    private DiaryEntryRepository diaryEntryRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private DiaryEntryMapper diaryEntryMapper;

    @Mock
    private WorkforceEntryMapper workforceEntryMapper;

    @Mock
    private MaterialUsageMapper materialUsageMapper;

    @Spy
    private ListMapper listMapper = new ListMapper();

    @InjectMocks
    private DiaryEntryServiceImpl diaryEntryService;

    private Project project;
    private DiaryEntry diaryEntry;
    private DiaryEntryDto diaryEntryDto;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(PROJECT_ID);
        project.setStatus(ProjectStatus.IN_PROGRESS);

        diaryEntry = new DiaryEntry();
        diaryEntry.setId(ENTRY_ID);
        diaryEntry.setDate(ENTRY_DATE);
        diaryEntry.setProject(project);

        diaryEntryDto = createDiaryEntryDto();
    }

    @Nested
    class Create {

        @Test
        void shouldCreateDiaryEntrySuccessfully() {
            when(projectRepository.findStatusById(PROJECT_ID)).thenReturn(ProjectStatus.IN_PROGRESS);
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE)).thenReturn(Optional.empty());
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryMapper.toEntity(diaryEntryDto)).thenReturn(diaryEntry);
            when(diaryEntryRepository.save(diaryEntry)).thenReturn(diaryEntry);
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(diaryEntryDto);

            DiaryEntryDto result = diaryEntryService.create(PROJECT_ID, diaryEntryDto);

            assertThat(result).isEqualTo(diaryEntryDto);
            verify(projectService).hasAccess(PROJECT_ID);
            verify(diaryEntryRepository).save(diaryEntry);
        }

        @Test
        void shouldCreateDiaryEntryWithWorkforceEntries() {
            WorkforceEntryDto workforceDto = new WorkforceEntryDto(null, "Mason", "John", "Doe", BigDecimal.valueOf(8));
            DiaryEntryDto dtoWithWorkforce = new DiaryEntryDto(
                    null, null, ENTRY_DATE, "Summary", List.of(workforceDto), null, null, null, null
            );
            WorkforceEntry workforceEntry = new WorkforceEntry();

            when(projectRepository.findStatusById(PROJECT_ID)).thenReturn(ProjectStatus.IN_PROGRESS);
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE)).thenReturn(Optional.empty());
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryMapper.toEntity(dtoWithWorkforce)).thenReturn(diaryEntry);
            when(workforceEntryMapper.toEntity(workforceDto)).thenReturn(workforceEntry);
            when(diaryEntryRepository.save(diaryEntry)).thenReturn(diaryEntry);
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(dtoWithWorkforce);

            DiaryEntryDto result = diaryEntryService.create(PROJECT_ID, dtoWithWorkforce);

            assertThat(result).isEqualTo(dtoWithWorkforce);
            assertThat(diaryEntry.getWorkforceEntries()).contains(workforceEntry);
            verify(workforceEntryMapper).toEntity(workforceDto);
        }

        @Test
        void shouldCreateDiaryEntryWithMaterialUsages() {
            MaterialUsageDto materialDto = new MaterialUsageDto(null, "Cement", BigDecimal.valueOf(50), "kg");
            DiaryEntryDto dtoWithMaterial = new DiaryEntryDto(
                    null, null, ENTRY_DATE, "Summary", null, List.of(materialDto), null, null, null
            );
            MaterialUsage materialUsage = new MaterialUsage();

            when(projectRepository.findStatusById(PROJECT_ID)).thenReturn(ProjectStatus.IN_PROGRESS);
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE)).thenReturn(Optional.empty());
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryMapper.toEntity(dtoWithMaterial)).thenReturn(diaryEntry);
            when(materialUsageMapper.toEntity(materialDto)).thenReturn(materialUsage);
            when(diaryEntryRepository.save(diaryEntry)).thenReturn(diaryEntry);
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(dtoWithMaterial);

            DiaryEntryDto result = diaryEntryService.create(PROJECT_ID, dtoWithMaterial);

            assertThat(result).isEqualTo(dtoWithMaterial);
            assertThat(diaryEntry.getMaterialUsages()).contains(materialUsage);
            verify(materialUsageMapper).toEntity(materialDto);
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryEntryService.create(PROJECT_ID, diaryEntryDto))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryRepository, never()).save(any());
        }

        @Test
        void shouldThrowExceptionWhenProjectIsCompleted() {
            when(projectRepository.findStatusById(PROJECT_ID)).thenReturn(ProjectStatus.COMPLETED);

            assertThatThrownBy(() -> diaryEntryService.create(PROJECT_ID, diaryEntryDto))
                    .isInstanceOf(ProjectStateException.class)
                    .hasMessageContaining("Cannot add diary entry to a completed project");

            verify(diaryEntryRepository, never()).save(any());
        }

        @Test
        void shouldThrowExceptionWhenEntryAlreadyExistsForDate() {
            when(projectRepository.findStatusById(PROJECT_ID)).thenReturn(ProjectStatus.IN_PROGRESS);
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE))
                    .thenReturn(Optional.of(diaryEntry));

            assertThatThrownBy(() -> diaryEntryService.create(PROJECT_ID, diaryEntryDto))
                    .isInstanceOf(DiaryEntryAlreadyExistsException.class)
                    .hasMessageContaining("Diary entry already exists");

            verify(diaryEntryRepository, never()).save(any());
        }
    }

    private DiaryEntryDto createDiaryEntryDto() {
        return new DiaryEntryDto(
                null,
                null,
                ENTRY_DATE,
                "Daily summary",
                null,
                null,
                null,
                null,
                null
        );
    }
}