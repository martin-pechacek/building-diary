package cz.mp.photos_service.service.impl;

import cz.mp.photos_service.dto.PhotoDto;
import cz.mp.photos_service.entity.Photo;
import cz.mp.photos_service.exception.InvalidFileTypeException;
import cz.mp.photos_service.exception.PhotoNotFoundException;
import cz.mp.photos_service.mapper.PhotoMapper;
import cz.mp.photos_service.repository.PhotoRepository;
import cz.mp.photos_service.service.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhotoServiceImplTest {

    private static final UUID DIARY_ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID PHOTO_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");
    private static final String OWNER_USER = "testuser";
    private static final String OTHER_USER = "otheruser";
    private static final String STORED_FILENAME = "abc-123.jpg";

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private PhotoMapper photoMapper;

    @InjectMocks
    private PhotoServiceImpl photoService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, authorities));
    }

    private Photo createPhoto() {
        Photo photo = new Photo();
        photo.setId(PHOTO_ID);
        photo.setDiaryEntryId(DIARY_ENTRY_ID);
        photo.setProjectId(PROJECT_ID);
        photo.setOwnerUserId(OWNER_USER);
        photo.setFilename("test.jpg");
        photo.setStoredFilename(STORED_FILENAME);
        photo.setContentType("image/jpeg");
        photo.setSize(1024L);
        photo.setDescription("Test photo");
        return photo;
    }

    private PhotoDto createPhotoDto() {
        return new PhotoDto(PHOTO_ID, DIARY_ENTRY_ID, "test.jpg", "image/jpeg", 1024L, "Test photo", Instant.now());
    }

    @Nested
    class Upload {

        // Valid JPEG header bytes
        private static final byte[] JPEG_HEADER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0};

        @BeforeEach
        void setUp() {
            setAuthentication(OWNER_USER, "ROLE_USER");
        }

        @Test
        void shouldUploadValidJpeg() {
            MockMultipartFile file = new MockMultipartFile("photo", "test.jpg", "image/jpeg", JPEG_HEADER);
            PhotoDto expectedDto = createPhotoDto();

            when(fileStorageService.save(file)).thenReturn(STORED_FILENAME);
            when(photoRepository.save(any(Photo.class))).thenAnswer(inv -> inv.getArgument(0));
            when(photoMapper.toDto(any(Photo.class))).thenReturn(expectedDto);

            PhotoDto result = photoService.upload(DIARY_ENTRY_ID, PROJECT_ID, file, "Test photo");

            assertThat(result.id()).isEqualTo(PHOTO_ID);
            verify(fileStorageService).save(file);
            verify(photoRepository).save(any(Photo.class));
        }

        @Test
        void shouldRejectInvalidFileType() {
            byte[] invalidContent = {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07};
            MockMultipartFile file = new MockMultipartFile("photo", "test.txt", "text/plain", invalidContent);

            assertThatThrownBy(() -> photoService.upload(DIARY_ENTRY_ID, PROJECT_ID, file, null))
                    .isInstanceOf(InvalidFileTypeException.class);

            verify(fileStorageService, never()).save(any());
        }
    }

    @Nested
    class GetFile {

        @Test
        void shouldReturnFileForOwner() {
            setAuthentication(OWNER_USER, "ROLE_USER");
            Photo photo = createPhoto();
            Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(photo));
            when(fileStorageService.load(STORED_FILENAME)).thenReturn(resource);

            Resource result = photoService.getFile(PHOTO_ID);

            assertThat(result).isEqualTo(resource);
        }

        @Test
        void shouldReturnFileForAdmin() {
            setAuthentication(OTHER_USER, "ROLE_ADMIN");
            Photo photo = createPhoto();
            Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(photo));
            when(fileStorageService.load(STORED_FILENAME)).thenReturn(resource);

            Resource result = photoService.getFile(PHOTO_ID);

            assertThat(result).isEqualTo(resource);
        }

        @Test
        void shouldThrowForNonOwner() {
            setAuthentication(OTHER_USER, "ROLE_USER");
            Photo photo = createPhoto();

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(photo));

            assertThatThrownBy(() -> photoService.getFile(PHOTO_ID))
                    .isInstanceOf(PhotoNotFoundException.class);
        }

        @Test
        void shouldThrowWhenPhotoNotFound() {
            setAuthentication(OWNER_USER, "ROLE_USER");

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> photoService.getFile(PHOTO_ID))
                    .isInstanceOf(PhotoNotFoundException.class);
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDeleteForOwner() {
            setAuthentication(OWNER_USER, "ROLE_USER");
            Photo photo = createPhoto();

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(photo));

            photoService.delete(PHOTO_ID);

            verify(fileStorageService).delete(STORED_FILENAME);
            verify(photoRepository).delete(photo);
        }

        @Test
        void shouldDeleteForAdmin() {
            setAuthentication(OTHER_USER, "ROLE_ADMIN");
            Photo photo = createPhoto();

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(photo));

            photoService.delete(PHOTO_ID);

            verify(fileStorageService).delete(STORED_FILENAME);
            verify(photoRepository).delete(photo);
        }

        @Test
        void shouldThrowDeleteForNonOwner() {
            setAuthentication(OTHER_USER, "ROLE_USER");
            Photo photo = createPhoto();

            when(photoRepository.findById(PHOTO_ID)).thenReturn(Optional.of(photo));

            assertThatThrownBy(() -> photoService.delete(PHOTO_ID))
                    .isInstanceOf(PhotoNotFoundException.class);

            verify(fileStorageService, never()).delete(any());
        }
    }

    @Nested
    class GetByDiaryEntryId {

        @Test
        void shouldReturnOnlyOwnedPhotos() {
            setAuthentication(OWNER_USER, "ROLE_USER");
            Photo ownedPhoto = createPhoto();
            Photo otherPhoto = createPhoto();
            otherPhoto.setOwnerUserId(OTHER_USER);
            PhotoDto expectedDto = createPhotoDto();

            when(photoRepository.findByDiaryEntryId(DIARY_ENTRY_ID)).thenReturn(List.of(ownedPhoto, otherPhoto));
            when(photoMapper.toDto(ownedPhoto)).thenReturn(expectedDto);

            List<PhotoDto> result = photoService.getByDiaryEntryId(DIARY_ENTRY_ID);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(PHOTO_ID);
        }

        @Test
        void shouldReturnAllPhotosForAdmin() {
            setAuthentication(OTHER_USER, "ROLE_ADMIN");
            Photo photo1 = createPhoto();
            Photo photo2 = createPhoto();
            photo2.setOwnerUserId(OTHER_USER);
            PhotoDto dto1 = createPhotoDto();
            PhotoDto dto2 = createPhotoDto();

            when(photoRepository.findByDiaryEntryId(DIARY_ENTRY_ID)).thenReturn(List.of(photo1, photo2));
            when(photoMapper.toDto(photo1)).thenReturn(dto1);
            when(photoMapper.toDto(photo2)).thenReturn(dto2);

            List<PhotoDto> result = photoService.getByDiaryEntryId(DIARY_ENTRY_ID);

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    class DeleteByDiaryEntryId {

        @Test
        void shouldDeleteAllPhotosForDiaryEntry() {
            Photo photo1 = createPhoto();
            Photo photo2 = createPhoto();
            photo2.setStoredFilename("def-456.jpg");

            when(photoRepository.findByDiaryEntryId(DIARY_ENTRY_ID)).thenReturn(List.of(photo1, photo2));

            photoService.deleteByDiaryEntryId(DIARY_ENTRY_ID);

            verify(fileStorageService).delete(STORED_FILENAME);
            verify(fileStorageService).delete("def-456.jpg");
            verify(photoRepository).deleteAllByDiaryEntryId(DIARY_ENTRY_ID);
        }
    }
}
