package cz.mp.photos_service.entity;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PhotoTest {

    private static final UUID ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final UUID DIARY_ENTRY_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    private static final UUID PROJECT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174002");

    @Nested
    class Construction {

        @Test
        void shouldCreateWithAllArgsConstructor() {
            Photo photo = new Photo(ID, DIARY_ENTRY_ID, PROJECT_ID, "user-kc-id",
                    "photo.jpg", "stored-uuid.jpg", "image/jpeg", 1024L, "A description");

            assertThat(photo.getId()).isEqualTo(ID);
            assertThat(photo.getDiaryEntryId()).isEqualTo(DIARY_ENTRY_ID);
            assertThat(photo.getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(photo.getOwnerUserId()).isEqualTo("user-kc-id");
            assertThat(photo.getFilename()).isEqualTo("photo.jpg");
            assertThat(photo.getStoredFilename()).isEqualTo("stored-uuid.jpg");
            assertThat(photo.getContentType()).isEqualTo("image/jpeg");
            assertThat(photo.getSize()).isEqualTo(1024L);
            assertThat(photo.getDescription()).isEqualTo("A description");
        }

        @Test
        void shouldCreateWithNoArgsConstructorAndSetters() {
            Photo photo = new Photo();
            photo.setId(ID);
            photo.setDiaryEntryId(DIARY_ENTRY_ID);
            photo.setProjectId(PROJECT_ID);
            photo.setOwnerUserId("user-kc-id");
            photo.setFilename("photo.jpg");
            photo.setStoredFilename("stored-uuid.jpg");
            photo.setContentType("image/jpeg");
            photo.setSize(2048L);
            photo.setDescription("Another description");

            assertThat(photo.getId()).isEqualTo(ID);
            assertThat(photo.getDiaryEntryId()).isEqualTo(DIARY_ENTRY_ID);
            assertThat(photo.getProjectId()).isEqualTo(PROJECT_ID);
            assertThat(photo.getOwnerUserId()).isEqualTo("user-kc-id");
            assertThat(photo.getFilename()).isEqualTo("photo.jpg");
            assertThat(photo.getStoredFilename()).isEqualTo("stored-uuid.jpg");
            assertThat(photo.getContentType()).isEqualTo("image/jpeg");
            assertThat(photo.getSize()).isEqualTo(2048L);
            assertThat(photo.getDescription()).isEqualTo("Another description");
        }

        @Test
        void shouldAllowNullDescription() {
            Photo photo = new Photo(ID, DIARY_ENTRY_ID, PROJECT_ID, "user-kc-id",
                    "photo.jpg", "stored-uuid.jpg", "image/jpeg", 512L, null);

            assertThat(photo.getDescription()).isNull();
        }
    }

    @Nested
    class InheritedAuditFields {

        @Test
        void shouldInheritCreatedAtAndUpdatedAt() {
            Photo photo = new Photo();
            Instant now = Instant.now();
            photo.setCreatedAt(now);
            photo.setUpdatedAt(now);

            assertThat(photo.getCreatedAt()).isEqualTo(now);
            assertThat(photo.getUpdatedAt()).isEqualTo(now);
        }

        @Test
        void shouldHaveNullAuditFieldsByDefault() {
            Photo photo = new Photo();

            assertThat(photo.getCreatedAt()).isNull();
            assertThat(photo.getUpdatedAt()).isNull();
        }
    }
}
