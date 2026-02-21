package cz.mp.photos_service.controller;

import cz.mp.photos_service.dto.PhotoDto;
import cz.mp.photos_service.exception.GlobalExceptionHandler;
import cz.mp.photos_service.exception.PhotoNotFoundException;
import cz.mp.photos_service.service.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static cz.mp.photos_service.controller.PhotoController.URL;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PhotoControllerTest {

    private static final UUID DIARY_ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID PHOTO_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");

    @Mock
    private PhotoService photoService;

    @InjectMocks
    private PhotoController photoController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(photoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private PhotoDto createPhotoDto() {
        return new PhotoDto(PHOTO_ID, DIARY_ENTRY_ID, "test.jpg", "image/jpeg", 1024L, "Test photo", Instant.now());
    }

    @Nested
    class UploadPhoto {

        @Test
        void shouldReturn201WhenUploaded() throws Exception {
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[]{1, 2, 3});
            PhotoDto photoDto = createPhotoDto();

            when(photoService.upload(eq(DIARY_ENTRY_ID), eq(PROJECT_ID), any(), eq("Test photo")))
                    .thenReturn(photoDto);

            mockMvc.perform(multipart(URL + "/projects/{projectId}/diary-entries/{diaryEntryId}",
                                    PROJECT_ID, DIARY_ENTRY_ID)
                            .file(file)
                            .param("description", "Test photo"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(PHOTO_ID.toString()))
                    .andExpect(jsonPath("$.filename").value("test.jpg"));
        }
    }

    @Nested
    class DownloadPhoto {

        @Test
        void shouldReturn200WithFile() throws Exception {
            Resource resource = new ByteArrayResource(new byte[]{1, 2, 3}) {
                @Override
                public String getFilename() {
                    return "test.jpg";
                }
            };

            when(photoService.getFile(PHOTO_ID)).thenReturn(resource);

            mockMvc.perform(get(URL + "/{id}", PHOTO_ID))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Disposition", "attachment; filename=\"test.jpg\""));
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            when(photoService.getFile(PHOTO_ID)).thenThrow(new PhotoNotFoundException("Photo not found: " + PHOTO_ID));

            mockMvc.perform(get(URL + "/{id}", PHOTO_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeletePhoto {

        @Test
        void shouldReturn204WhenDeleted() throws Exception {
            mockMvc.perform(delete(URL + "/{id}", PHOTO_ID))
                    .andExpect(status().isNoContent());

            verify(photoService).delete(PHOTO_ID);
        }
    }

    @Nested
    class GetByDiaryEntryId {

        @Test
        void shouldReturn200WithPhotoList() throws Exception {
            PhotoDto photoDto = createPhotoDto();

            when(photoService.getByDiaryEntryId(DIARY_ENTRY_ID)).thenReturn(List.of(photoDto));

            mockMvc.perform(get(URL + "/diary-entries/{diaryEntryId}", DIARY_ENTRY_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(PHOTO_ID.toString()))
                    .andExpect(jsonPath("$[0].diaryEntryId").value(DIARY_ENTRY_ID.toString()));
        }
    }

    @Nested
    class DeleteByDiaryEntryId {

        @Test
        void shouldReturn204WhenDeleted() throws Exception {
            mockMvc.perform(delete(URL + "/diary-entries/{diaryEntryId}", DIARY_ENTRY_ID))
                    .andExpect(status().isNoContent());

            verify(photoService).deleteByDiaryEntryId(DIARY_ENTRY_ID);
        }
    }
}
