package cz.mp.construction_site_diary.controller;

import cz.mp.construction_site_diary.dto.DiaryEntryDto;
import cz.mp.construction_site_diary.exception.DiaryEntryAlreadyExistsException;
import cz.mp.construction_site_diary.exception.DiaryEntryNotFoundException;
import cz.mp.construction_site_diary.exception.GlobalExceptionHandler;
import cz.mp.construction_site_diary.exception.ProjectNotFoundException;
import cz.mp.construction_site_diary.exception.ProjectStateException;
import cz.mp.construction_site_diary.facade.DiaryEntryFacade;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static cz.mp.construction_site_diary.util.JsonTestUtil.toJson;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DiaryEntryControllerTest {

    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final LocalDate ENTRY_DATE = LocalDate.of(2024, 1, 15);
    private static final String BASE_URL = "/api/v1/projects/" + PROJECT_ID + "/diaryEntries";

    private MockMvc mockMvc;

    @Mock
    private DiaryEntryFacade diaryEntryFacade;

    @InjectMocks
    private DiaryEntryController diaryEntryController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(diaryEntryController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class Create {

        @Test
        void shouldCreateDiaryEntrySuccessfully() throws Exception {
            DiaryEntryDto request = createDiaryEntryRequest();
            DiaryEntryDto response = createDiaryEntryResponse();

            when(diaryEntryFacade.createEntry(eq(PROJECT_ID), any())).thenReturn(response);

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(ENTRY_ID.toString()))
                    .andExpect(jsonPath("$.date").value(ENTRY_DATE.toString()));
        }

        @Test
        void shouldReturnBadRequestWhenDateIsNull() throws Exception {
            DiaryEntryDto request = new DiaryEntryDto(
                    null, null, null, "Summary", null, null, null, null, null, null, null
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(diaryEntryFacade, never()).createEntry(any(), any());
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            DiaryEntryDto request = createDiaryEntryRequest();

            when(diaryEntryFacade.createEntry(eq(PROJECT_ID), any()))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnConflictWhenEntryAlreadyExists() throws Exception {
            DiaryEntryDto request = createDiaryEntryRequest();

            when(diaryEntryFacade.createEntry(eq(PROJECT_ID), any()))
                    .thenThrow(new DiaryEntryAlreadyExistsException("Diary entry already exists"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class GetDiaryEntry {

        @Test
        void shouldReturnDiaryEntrySuccessfully() throws Exception {
            DiaryEntryDto response = createDiaryEntryResponse();

            when(diaryEntryFacade.getEntry(PROJECT_ID, ENTRY_DATE)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/" + ENTRY_DATE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ENTRY_ID.toString()))
                    .andExpect(jsonPath("$.date").value(ENTRY_DATE.toString()));
        }

        @Test
        void shouldReturnNotFoundWhenEntryDoesNotExist() throws Exception {
            when(diaryEntryFacade.getEntry(PROJECT_ID, ENTRY_DATE))
                    .thenThrow(new DiaryEntryNotFoundException("Diary entry not found"));

            mockMvc.perform(get(BASE_URL + "/" + ENTRY_DATE))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            when(diaryEntryFacade.getEntry(PROJECT_ID, ENTRY_DATE))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(get(BASE_URL + "/" + ENTRY_DATE))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class GetAllDiaryEntries {

        @Test
        void shouldReturnPaginatedDiaryEntries() throws Exception {
            DiaryEntryDto response = createDiaryEntryResponse();
            Page<DiaryEntryDto> page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            when(diaryEntryFacade.getEntries(eq(PROJECT_ID), any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(ENTRY_ID.toString()))
                    .andExpect(jsonPath("$.content[0].date").value(ENTRY_DATE.toString()))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldReturnEmptyPageWhenNoEntries() throws Exception {
            Page<DiaryEntryDto> emptyPage = Page.empty(PageRequest.of(0, 20));

            when(diaryEntryFacade.getEntries(eq(PROJECT_ID), any())).thenReturn(emptyPage);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldReturnNotFoundWhenProjectDoesNotExist() throws Exception {
            when(diaryEntryFacade.getEntries(eq(PROJECT_ID), any()))
                    .thenThrow(new ProjectNotFoundException("Project not found: " + PROJECT_ID));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldPassPageParameter() throws Exception {
            Page<DiaryEntryDto> emptyPage = Page.empty(PageRequest.of(2, 20));

            when(diaryEntryFacade.getEntries(eq(PROJECT_ID), eq(PageRequest.of(2, 20))))
                    .thenReturn(emptyPage);

            mockMvc.perform(get(BASE_URL + "?page=2"))
                    .andExpect(status().isOk());

            verify(diaryEntryFacade).getEntries(PROJECT_ID, PageRequest.of(2, 20));
        }
    }

    @Nested
    class Update {

        @Test
        void shouldUpdateDiaryEntrySuccessfully() throws Exception {
            DiaryEntryDto request = createDiaryEntryRequest();
            DiaryEntryDto response = createDiaryEntryResponse();

            when(diaryEntryFacade.updateEntry(eq(ENTRY_ID), any())).thenReturn(response);

            mockMvc.perform(put(BASE_URL + "/" + ENTRY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(ENTRY_ID.toString()))
                    .andExpect(jsonPath("$.date").value(ENTRY_DATE.toString()));
        }

        @Test
        void shouldReturnBadRequestWhenDateIsNull() throws Exception {
            DiaryEntryDto request = new DiaryEntryDto(
                    null, null, null, "Summary", null, null, null, null, null, null, null
            );

            mockMvc.perform(put(BASE_URL + "/" + ENTRY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(diaryEntryFacade, never()).updateEntry(any(), any());
        }

        @Test
        void shouldReturnNotFoundWhenEntryDoesNotExist() throws Exception {
            DiaryEntryDto request = createDiaryEntryRequest();

            when(diaryEntryFacade.updateEntry(eq(ENTRY_ID), any()))
                    .thenThrow(new DiaryEntryNotFoundException("Diary entry not found: " + ENTRY_ID));

            mockMvc.perform(put(BASE_URL + "/" + ENTRY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnBadRequestWhenProjectCompleted() throws Exception {
            DiaryEntryDto request = createDiaryEntryRequest();

            when(diaryEntryFacade.updateEntry(eq(ENTRY_ID), any()))
                    .thenThrow(new ProjectStateException("Cannot add or modify diary entry in a completed project"));

            mockMvc.perform(put(BASE_URL + "/" + ENTRY_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    private DiaryEntryDto createDiaryEntryRequest() {
        return new DiaryEntryDto(
                null,
                null,
                ENTRY_DATE,
                "Daily summary",
                "Sunny",
                22.5,
                null,
                null,
                null,
                null,
                null
        );
    }

    private DiaryEntryDto createDiaryEntryResponse() {
        return new DiaryEntryDto(
                ENTRY_ID,
                PROJECT_ID,
                ENTRY_DATE,
                "Daily summary",
                "Sunny",
                22.5,
                null,
                null,
                null,
                null,
                null
        );
    }

}
