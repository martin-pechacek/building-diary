package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.entity.DiaryEntry;
import cz.mp.construction_site_diary.entity.Project;
import cz.mp.construction_site_diary.exception.DiaryEntryAlreadyExistsException;
import cz.mp.construction_site_diary.exception.DiaryEntryNotFoundException;
import cz.mp.construction_site_diary.exception.ProjectNotFoundException;
import cz.mp.construction_site_diary.exception.ProjectStateException;
import cz.mp.construction_site_diary.mapper.DiaryEntryMapper;
import cz.mp.construction_site_diary.mapper.MaterialUsageMapper;
import cz.mp.construction_site_diary.mapper.WorkforceEntryMapper;
import cz.mp.construction_site_diary.repository.DiaryEntryRepository;
import cz.mp.construction_site_diary.repository.ProjectRepository;
import cz.mp.construction_site_diary.service.ProjectService;
import cz.mp.construction_site_diary.service.SecurityService;
import cz.mp.construction_site_diary.service.WeatherService;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import org.junit.jupiter.api.BeforeEach;
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
    private SecurityService securityService;

    @Mock
    private WeatherService weatherService;

    @Mock
    private DiaryEntryMapper diaryEntryMapper;

    @Mock
    private WorkforceEntryMapper workforceEntryMapper;

    @Mock
    private MaterialUsageMapper materialUsageMapper;

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
            when(securityService.isEmailVerified()).thenReturn(true);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE)).thenReturn(Optional.empty());
            when(projectRepository.getReferenceById(PROJECT_ID)).thenReturn(project);
            when(diaryEntryMapper.toEntity(diaryEntryDto, workforceEntryMapper, materialUsageMapper)).thenReturn(diaryEntry);
            when(diaryEntryRepository.save(diaryEntry)).thenReturn(diaryEntry);
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(diaryEntryDto);

            DiaryEntryDto result = diaryEntryService.create(PROJECT_ID, diaryEntryDto);

            assertThat(result).isEqualTo(diaryEntryDto);
            verify(projectService).hasAccess(PROJECT_ID);
            verify(diaryEntryMapper).toEntity(diaryEntryDto, workforceEntryMapper, materialUsageMapper);
            verify(diaryEntryRepository).save(diaryEntry);
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            when(securityService.isEmailVerified()).thenReturn(true);
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryEntryService.create(PROJECT_ID, diaryEntryDto))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryRepository, never()).save(any());
        }

        @Test
        void shouldThrowExceptionWhenProjectIsCompleted() {
            when(securityService.isEmailVerified()).thenReturn(true);
            Project completedProject = new Project();
            completedProject.setId(PROJECT_ID);
            completedProject.setStatus(ProjectStatus.COMPLETED);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(completedProject));

            assertThatThrownBy(() -> diaryEntryService.create(PROJECT_ID, diaryEntryDto))
                    .isInstanceOf(ProjectStateException.class)
                    .hasMessageContaining("Cannot add or modify diary entry in a completed project");

            verify(diaryEntryRepository, never()).save(any());
        }

        @Test
        void shouldThrowExceptionWhenEntryAlreadyExistsForDate() {
            when(securityService.isEmailVerified()).thenReturn(true);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE))
                    .thenReturn(Optional.of(diaryEntry));

            assertThatThrownBy(() -> diaryEntryService.create(PROJECT_ID, diaryEntryDto))
                    .isInstanceOf(DiaryEntryAlreadyExistsException.class)
                    .hasMessageContaining("Diary entry already exists");

            verify(diaryEntryRepository, never()).save(any());
        }
    }

    @Nested
    class GetByProjectIdAndDate {

        @Test
        void shouldReturnDiaryEntrySuccessfully() {
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE))
                    .thenReturn(Optional.of(diaryEntry));
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(diaryEntryDto);

            DiaryEntryDto result = diaryEntryService.getByProjectIdAndDate(PROJECT_ID, ENTRY_DATE);

            assertThat(result).isEqualTo(diaryEntryDto);
            verify(projectService).hasAccess(PROJECT_ID);
            verify(diaryEntryRepository).findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE);
        }

        @Test
        void shouldThrowExceptionWhenEntryNotFound() {
            when(diaryEntryRepository.findByProjectIdAndDate(PROJECT_ID, ENTRY_DATE))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> diaryEntryService.getByProjectIdAndDate(PROJECT_ID, ENTRY_DATE))
                    .isInstanceOf(DiaryEntryNotFoundException.class)
                    .hasMessageContaining("Diary entry not found");

            verify(projectService).hasAccess(PROJECT_ID);
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryEntryService.getByProjectIdAndDate(PROJECT_ID, ENTRY_DATE))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryRepository, never()).findByProjectIdAndDate(any(), any());
        }
    }

    @Nested
    class GetAllByProjectId {

        @Test
        void shouldReturnPaginatedDiaryEntries() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<DiaryEntry> entryPage = new PageImpl<>(List.of(diaryEntry), pageable, 1);

            when(diaryEntryRepository.findByProjectIdOrderByDateDesc(PROJECT_ID, pageable))
                    .thenReturn(entryPage);
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(diaryEntryDto);

            Page<DiaryEntryDto> result = diaryEntryService.getAllByProjectId(PROJECT_ID, pageable);

            assertThat(result.getContent()).containsExactly(diaryEntryDto);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(projectService).hasAccess(PROJECT_ID);
        }

        @Test
        void shouldReturnEmptyPageWhenNoEntries() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<DiaryEntry> emptyPage = Page.empty(pageable);

            when(diaryEntryRepository.findByProjectIdOrderByDateDesc(PROJECT_ID, pageable))
                    .thenReturn(emptyPage);

            Page<DiaryEntryDto> result = diaryEntryService.getAllByProjectId(PROJECT_ID, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(projectService).hasAccess(PROJECT_ID);
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            Pageable pageable = PageRequest.of(0, 20);
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryEntryService.getAllByProjectId(PROJECT_ID, pageable))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryRepository, never()).findByProjectIdOrderByDateDesc(any(), any());
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateDiaryEntrySuccessfully() {
            when(securityService.isEmailVerified()).thenReturn(true);
            when(diaryEntryRepository.findById(ENTRY_ID)).thenReturn(Optional.of(diaryEntry));
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(project));
            when(diaryEntryMapper.toDto(diaryEntry)).thenReturn(diaryEntryDto);

            DiaryEntryDto result = diaryEntryService.update(ENTRY_ID, diaryEntryDto);

            assertThat(result).isEqualTo(diaryEntryDto);
            verify(projectService).hasAccess(PROJECT_ID);
            verify(diaryEntryMapper).updateEntity(diaryEntryDto, diaryEntry, workforceEntryMapper, materialUsageMapper);
        }

        @Test
        void shouldThrowExceptionWhenEntryNotFound() {
            when(diaryEntryRepository.findById(ENTRY_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> diaryEntryService.update(ENTRY_ID, diaryEntryDto))
                    .isInstanceOf(DiaryEntryNotFoundException.class)
                    .hasMessageContaining("Diary entry not found: " + ENTRY_ID);

            verify(diaryEntryMapper, never()).updateEntity(any(), any(), any(), any());
        }

        @Test
        void shouldThrowExceptionWhenNoAccess() {
            when(securityService.isEmailVerified()).thenReturn(true);
            when(diaryEntryRepository.findById(ENTRY_ID)).thenReturn(Optional.of(diaryEntry));
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).hasAccess(PROJECT_ID);

            assertThatThrownBy(() -> diaryEntryService.update(ENTRY_ID, diaryEntryDto))
                    .isInstanceOf(ProjectNotFoundException.class);

            verify(diaryEntryMapper, never()).updateEntity(any(), any(), any(), any());
        }

        @Test
        void shouldThrowExceptionWhenProjectIsCompleted() {
            when(securityService.isEmailVerified()).thenReturn(true);
            when(diaryEntryRepository.findById(ENTRY_ID)).thenReturn(Optional.of(diaryEntry));
            Project completedProject = new Project();
            completedProject.setId(PROJECT_ID);
            completedProject.setStatus(ProjectStatus.COMPLETED);
            when(projectRepository.findById(PROJECT_ID)).thenReturn(Optional.of(completedProject));

            assertThatThrownBy(() -> diaryEntryService.update(ENTRY_ID, diaryEntryDto))
                    .isInstanceOf(ProjectStateException.class)
                    .hasMessageContaining("Cannot add or modify diary entry in a completed project");

            verify(diaryEntryMapper, never()).updateEntity(any(), any(), any(), any());
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
                null,
                null,
                null
        );
    }
}