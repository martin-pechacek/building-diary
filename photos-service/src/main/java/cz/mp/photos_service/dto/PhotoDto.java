package cz.mp.photos_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Schema(description = "Photo data")
public record PhotoDto(
        @Schema(description = "Photo ID")
        @JsonProperty(access = READ_ONLY)
        UUID id,

        @Schema(description = "Diary entry ID")
        @JsonProperty(access = READ_ONLY)
        UUID diaryEntryId,

        @Schema(description = "Original filename")
        @JsonProperty(access = READ_ONLY)
        String filename,

        @Schema(description = "Content type")
        @JsonProperty(access = READ_ONLY)
        String contentType,

        @Schema(description = "File size in bytes")
        @JsonProperty(access = READ_ONLY)
        Long size,

        @Schema(description = "Photo description")
        String description,

        @Schema(description = "Upload timestamp")
        @JsonProperty(access = READ_ONLY)
        Instant createdAt
) {
}
