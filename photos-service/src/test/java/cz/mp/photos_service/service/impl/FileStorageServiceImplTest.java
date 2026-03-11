package cz.mp.photos_service.service.impl;

import cz.mp.photos_service.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageServiceImpl(tempDir);
    }

    @Nested
    class Save {

        @Test
        void shouldSaveFileAndReturnStoredFilename() {
            MockMultipartFile file = new MockMultipartFile("photo", "image.jpg", "image/jpeg", new byte[]{1, 2, 3});

            String storedFilename = fileStorageService.save(file);

            assertThat(storedFilename).endsWith(".jpg");
            assertThat(tempDir.resolve(storedFilename)).exists();
        }

        @Test
        void shouldPreserveFileExtension() {
            MockMultipartFile file = new MockMultipartFile("photo", "picture.png", "image/png", new byte[]{1, 2, 3});

            String storedFilename = fileStorageService.save(file);

            assertThat(storedFilename).endsWith(".png");
        }

        @Test
        void shouldHandleFilenameWithNoExtension() {
            MockMultipartFile file = new MockMultipartFile("photo", "noextension", "image/jpeg", new byte[]{1, 2, 3});

            String storedFilename = fileStorageService.save(file);

            assertThat(storedFilename).isNotEmpty();
            assertThat(tempDir.resolve(storedFilename)).exists();
        }

        @Test
        void shouldThrowFileStorageExceptionOnIoError() {
            MockMultipartFile badFile = new MockMultipartFile("photo", "image.jpg", "image/jpeg", new byte[]{1, 2, 3}) {
                @Override
                public java.io.InputStream getInputStream() throws IOException {
                    throw new IOException("Simulated I/O error");
                }
            };

            assertThatThrownBy(() -> fileStorageService.save(badFile))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("image.jpg");
        }
    }

    @Nested
    class Load {

        @Test
        void shouldReturnResourceForExistingFile() throws IOException {
            Path file = tempDir.resolve("test.jpg");
            Files.write(file, new byte[]{1, 2, 3});

            Resource resource = fileStorageService.load("test.jpg");

            assertThat(resource.exists()).isTrue();
            assertThat(resource.getFilename()).isEqualTo("test.jpg");
        }

        @Test
        void shouldThrowFileStorageExceptionForMissingFile() {
            assertThatThrownBy(() -> fileStorageService.load("nonexistent.jpg"))
                    .isInstanceOf(FileStorageException.class)
                    .hasMessageContaining("nonexistent.jpg");
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDeleteExistingFile() throws IOException {
            Path file = tempDir.resolve("to-delete.jpg");
            Files.write(file, new byte[]{1, 2, 3});

            fileStorageService.delete("to-delete.jpg");

            assertThat(file).doesNotExist();
        }

        @Test
        void shouldNotThrowWhenFileDoesNotExist() {
            fileStorageService.delete("nonexistent.jpg");
        }
    }
}