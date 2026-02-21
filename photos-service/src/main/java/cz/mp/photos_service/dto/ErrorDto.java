package cz.mp.photos_service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Error response")
public record
ErrorDto(
        @Schema(description = "Error message", example = "Validation failed")
        String message,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Timestamp of the error")
        Instant timestamp,

        @Schema(description = "List of validation errors", nullable = true)
        List<String> errors
) {
    public ErrorDto(String message, int status) {
        this(message, status, Instant.now(), null);
    }

    public ErrorDto(String message, int status, List<String> errors) {
        this(message, status, Instant.now(), errors);
    }
}
