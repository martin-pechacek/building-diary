package cz.mp.building_diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Schema(description = "Material usage data")
public record MaterialUsageDto(
        @Schema(description = "Material usage ID")
        @JsonProperty(access = READ_ONLY)
        UUID id,

        @Schema(description = "Material name", example = "Cement")
        @NotBlank(message = "Material name is required")
        String materialName,

        @Schema(description = "Quantity used", example = "50.5")
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than 0")
        BigDecimal quantity,

        @Schema(description = "Unit of measurement", example = "kg")
        @NotBlank(message = "Unit is required")
        String unit
) {
}