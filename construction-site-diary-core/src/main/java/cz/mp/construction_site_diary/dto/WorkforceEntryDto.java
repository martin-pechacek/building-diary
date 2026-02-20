package cz.mp.construction_site_diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Schema(description = "Workforce entry data")
public record WorkforceEntryDto(
        @Schema(description = "Workforce entry ID")
        @JsonProperty(access = READ_ONLY)
        UUID id,

        @Schema(description = "Worker role", example = "Mason")
        @NotBlank(message = "Role is required")
        String role,

        @Schema(description = "Worker first name", example = "John")
        @NotBlank(message = "First name is required")
        String firstname,

        @Schema(description = "Worker last name", example = "Doe")
        @NotBlank(message = "Last name is required")
        String lastname,

        @Schema(description = "Working hours", example = "8.5")
        @Positive(message = "Working hours must be greater than 0")
        BigDecimal workingHours
) {
}