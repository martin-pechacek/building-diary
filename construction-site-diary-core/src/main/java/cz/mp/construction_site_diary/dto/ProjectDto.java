package cz.mp.construction_site_diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mp.construction_site_diary.statemachine.states.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Schema(description = "Project data")
public record ProjectDto(
        @Schema(description = "Project ID")
        @JsonProperty(access = READ_ONLY)
        UUID id,

        @Schema(description = "Project name", example = "Family House Construction")
        @NotBlank(message = "Project name is required")
        @Size(max = 255, message = "Project name must not exceed 255 characters")
        String name,

        @Schema(description = "Project description", example = "Construction of a two-story family house")
        String description,

        @Schema(description = "Issued permit number", example = "SP/2024/001234")
        @Size(max = 100, message = "Permit number must not exceed 100 characters")
        String permitNumber,

        @Schema(description = "Construction site address")
        @Valid
        @NotNull(message = "Construction site address is required")
        AddressDto constructionSiteAddress,

        @Schema(description = "Project status")
        @JsonProperty(access = READ_ONLY)
        ProjectStatus status,

        @Schema(description = "Project owner ID")
        @JsonProperty(access = READ_ONLY)
        UUID createdById,

        @Schema(description = "Construction manager ID")
        UUID constructionManagerId,

        @Schema(description = "Project start date")
        @JsonProperty(access = READ_ONLY)
        LocalDate startDate,

        @Schema(description = "Project end date")
        @JsonProperty(access = READ_ONLY)
        LocalDate endDate,

        @Schema(description = "Creation timestamp")
        @JsonProperty(access = READ_ONLY)
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        @JsonProperty(access = READ_ONLY)
        Instant updatedAt
) {
}