package cz.mp.building_diary.controller;

import cz.mp.building_diary.dto.AddressDto;
import cz.mp.building_diary.dto.DiaryEntryDto;
import cz.mp.building_diary.dto.ProjectDto;
import cz.mp.building_diary.entity.Country;
import cz.mp.building_diary.exception.DiaryEntryAlreadyExistsException;
import cz.mp.building_diary.exception.DiaryEntryNotFoundException;
import cz.mp.building_diary.exception.ExportException;
import cz.mp.building_diary.exception.GlobalExceptionHandler;
import cz.mp.building_diary.exception.ProjectNotFoundException;
import cz.mp.building_diary.exception.ProjectStateException;
import cz.mp.building_diary.service.DiaryEntryService;
import cz.mp.building_diary.service.DiaryExportService;
import cz.mp.building_diary.service.ProjectService;
import cz.mp.building_diary.service.export.ExportFormat;
import cz.mp.building_diary.statemachine.states.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static cz.mp.building_diary.controller.ProjectController.URL;
import static cz.mp.building_diary.util.JsonTestUtil.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String PROJECT_NAME = "Test Project";

    private MockMvc mockMvc;

    @Mock
    private ProjectService projectService;

    @Mock
    private DiaryEntryService diaryEntryService;

    @Mock
    private DiaryExportService diaryExportService;

    @InjectMocks
    private ProjectController projectController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(projectController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class Create {

        @Test
        void shouldCreateProjectSuccessfully() throws Exception {
            ProjectDto request = createProjectRequest();
            ProjectDto response = createProjectResponse();

            when(projectService.create(any())).thenReturn(response);

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()))
                    .andExpect(jsonPath("$.name").value(PROJECT_NAME));
        }

        @Test
        void shouldReturnBadRequestWhenNameIsBlank() throws Exception {
            ProjectDto request = new ProjectDto(
                    null, "", null, null,
                    createAddressDto(), null, null, null, null, null, null, null
            );

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).create(any());
        }

        @Test
        void shouldReturnBadRequestWhenAddressIsNull() throws Exception {
            ProjectDto request = new ProjectDto(
                    null, PROJECT_NAME, null, null,
                    null, null, null, null, null, null, null, null
            );

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).create(any());
        }

        @Test
        void shouldReturnBadRequestWhenAddressInvalid() throws Exception {
            AddressDto invalidAddress = new AddressDto(null, null, null, "Praha", "11000", Country.CZ);
            ProjectDto request = new ProjectDto(
                    null, PROJECT_NAME, null, null,
                    invalidAddress, null, null, null, null, null, null, null
            );

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).create(any());
        }
    }

    @Nested
    class GetById {

        @Test
        void shouldReturnProjectSuccessfully() throws Exception {
            ProjectDto response = createProjectResponse();

            when(projectService.getById(PROJECT_ID)).thenReturn(response);

            mockMvc.perform(get(URL + "/" + PROJECT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()))
                    .andExpect(jsonPath("$.name").value(PROJECT_NAME));
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            when(projectService.getById(PROJECT_ID))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(get(URL + "/" + PROJECT_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetAll {

        @Test
        void shouldReturnAllProjects() throws Exception {
            ProjectDto response = createProjectResponse();

            when(projectService.getAll()).thenReturn(List.of(response));

            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(PROJECT_ID.toString()))
                    .andExpect(jsonPath("$[0].name").value(PROJECT_NAME));
        }

        @Test
        void shouldReturnEmptyListWhenNoProjects() throws Exception {
            when(projectService.getAll()).thenReturn(List.of());

            mockMvc.perform(get(URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateProjectSuccessfully() throws Exception {
            ProjectDto request = createProjectRequest();
            ProjectDto response = createProjectResponse();

            when(projectService.update(eq(PROJECT_ID), any())).thenReturn(response);

            mockMvc.perform(put(URL + "/" + PROJECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()));
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            ProjectDto request = createProjectRequest();

            when(projectService.update(eq(PROJECT_ID), any()))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(put(URL + "/" + PROJECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnBadRequestWhenValidationFails() throws Exception {
            ProjectDto request = new ProjectDto(
                    null, "", null, null,
                    createAddressDto(), null, null, null, null, null, null, null
            );

            mockMvc.perform(put(URL + "/" + PROJECT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(projectService, never()).update(any(), any());
        }
    }

    @Nested
    class Archive {

        @Test
        void shouldArchiveProjectSuccessfully() throws Exception {
            mockMvc.perform(delete(URL + "/" + PROJECT_ID))
                    .andExpect(status().isNoContent());

            verify(projectService).archive(PROJECT_ID);
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            doThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID))
                    .when(projectService).archive(PROJECT_ID);

            mockMvc.perform(delete(URL + "/" + PROJECT_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Start {

        @Test
        void shouldStartProjectSuccessfully() throws Exception {
            ProjectDto response = createProjectResponse();

            when(projectService.start(PROJECT_ID)).thenReturn(response);

            mockMvc.perform(post(URL + "/" + PROJECT_ID + "/start"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()));

            verify(projectService).start(PROJECT_ID);
        }

        @Test
        void shouldReturnBadRequestWhenRequirementsNotMet() throws Exception {
            when(projectService.start(PROJECT_ID))
                    .thenThrow(new ProjectStateException("Cannot start project"));

            mockMvc.perform(post(URL + "/" + PROJECT_ID + "/start"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            when(projectService.start(PROJECT_ID))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(post(URL + "/" + PROJECT_ID + "/start"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Complete {

        @Test
        void shouldCompleteProjectSuccessfully() throws Exception {
            ProjectDto response = createProjectResponse();

            when(projectService.complete(PROJECT_ID)).thenReturn(response);

            mockMvc.perform(post(URL + "/" + PROJECT_ID + "/complete"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(PROJECT_ID.toString()));

            verify(projectService).complete(PROJECT_ID);
        }

        @Test
        void shouldReturnBadRequestWhenRequirementsNotMet() throws Exception {
            when(projectService.complete(PROJECT_ID))
                    .thenThrow(new ProjectStateException("Cannot complete project"));

            mockMvc.perform(post(URL + "/" + PROJECT_ID + "/complete"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            when(projectService.complete(PROJECT_ID))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(post(URL + "/" + PROJECT_ID + "/complete"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class Export {

        @Test
        void shouldExportToCsvSuccessfully() throws Exception {
            byte[] csvData = "Date,Weather,Temperature\n2024-01-15,Sunny,22.5".getBytes();
            when(diaryExportService.export(PROJECT_ID, ExportFormat.CSV)).thenReturn(csvData);

            mockMvc.perform(get(URL + "/" + PROJECT_ID + "/export/csv"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/csv"))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"diary-export-" + PROJECT_ID + ".csv\""));

            verify(diaryExportService).export(PROJECT_ID, ExportFormat.CSV);
        }

        @Test
        void shouldExportToPdfSuccessfully() throws Exception {
            byte[] pdfData = "%PDF-1.4 test content".getBytes();
            when(diaryExportService.export(PROJECT_ID, ExportFormat.PDF)).thenReturn(pdfData);

            mockMvc.perform(get(URL + "/" + PROJECT_ID + "/export/pdf"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                    .andExpect(header().string("Content-Disposition",
                            "attachment; filename=\"diary-export-" + PROJECT_ID + ".pdf\""));

            verify(diaryExportService).export(PROJECT_ID, ExportFormat.PDF);
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            when(diaryExportService.export(PROJECT_ID, ExportFormat.CSV))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(get(URL + "/" + PROJECT_ID + "/export/csv"))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnInternalServerErrorWhenExportFails() throws Exception {
            when(diaryExportService.export(PROJECT_ID, ExportFormat.PDF))
                    .thenThrow(new ExportException("Failed to export", new RuntimeException()));

            mockMvc.perform(get(URL + "/" + PROJECT_ID + "/export/pdf"))
                    .andExpect(status().isInternalServerError());
        }
    }

    private ProjectDto createProjectRequest() {
        return new ProjectDto(
                null,
                PROJECT_NAME,
                "Test Description",
                "BP-2024-001",
                createAddressDto(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private ProjectDto createProjectResponse() {
        return new ProjectDto(
                PROJECT_ID,
                PROJECT_NAME,
                "Test Description",
                "BP-2024-001",
                createAddressDto(),
                ProjectStatus.PLANNING,
                UUID.randomUUID(),
                null,
                null,
                null,
                null,
                null
        );
    }

    private AddressDto createAddressDto() {
        return new AddressDto("1234/5", null, null, "Praha", "11000", Country.CZ);
    }

}