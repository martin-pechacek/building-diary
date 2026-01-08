package cz.mp.building_diary.dto;

import cz.mp.building_diary.enums.Country;
import cz.mp.building_diary.validation.ValidAddress;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Address information for construction site")
@ValidAddress
public record AddressDto(
        @Schema(description = "Parcel number", example = "1234/5")
        @Size(max = 10, message = "Parcel number must not exceed 10 characters")
        String parcelNumber,

        @Schema(description = "Street name", example = "Hlavní")
        @Size(max = 255, message = "Street must not exceed 255 characters")
        String street,

        @Schema(description = "Street number", example = "123")
        @Size(max = 10, message = "Street number must not exceed 10 characters")
        String streetNumber,

        @Schema(description = "City", example = "Praha")
        @NotBlank(message = "City is required")
        @Size(max = 255, message = "City must not exceed 255 characters")
        String city,

        @Schema(description = "Postal code", example = "11000")
        @NotBlank(message = "Postal code is required")
        @Size(min = 5, max = 5, message = "Postal code must be 5 characters long")
        String postalCode,

        @Schema(description = "Country", example = "CZ")
        @NotNull(message = "Country is required")
        Country country
) {
}