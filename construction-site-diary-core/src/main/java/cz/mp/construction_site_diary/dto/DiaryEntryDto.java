package cz.mp.construction_site_diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mp.construction_site_diary.validation.NotFutureDate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Schema(description = "Diary entry data")
public record DiaryEntryDto(
        @Schema(description = "Diary entry ID")
        @JsonProperty(access = READ_ONLY)
        UUID id,

        @Schema(description = "Project ID")
        @JsonProperty(access = READ_ONLY)
        UUID projectId,

        @Schema(description = "Entry date", example = "2024-01-15")
        @NotNull(message = "Date is required")
        @NotFutureDate
        LocalDate date,

        @Schema(description = "Daily summary")
        String summary,

        @Schema(description = "Weather condition", example = "Sunny")
        String weatherCondition,

        @Schema(description = "Temperature in Celsius", example = "22.5")
        Double temperature,

        @Schema(description = "Workforce entries")
        @Valid
        List<WorkforceEntryDto> workforceEntries,

        @Schema(description = "Material usages")
        @Valid
        List<MaterialUsageDto> materialUsages,

        @Schema(description = "Created by user ID")
        @JsonProperty(access = READ_ONLY)
        UUID createdById,

        @Schema(description = "Creation timestamp")
        @JsonProperty(access = READ_ONLY)
        Instant createdAt,

        @Schema(description = "Last update timestamp")
        @JsonProperty(access = READ_ONLY)
        Instant updatedAt
) {
}