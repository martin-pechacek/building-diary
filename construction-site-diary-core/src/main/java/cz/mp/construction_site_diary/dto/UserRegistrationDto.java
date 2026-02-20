package cz.mp.construction_site_diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import cz.mp.construction_site_diary.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Schema(description = "User registration")
public record UserRegistrationDto(
        @Schema(description = "Keycloak user ID")
        @JsonProperty(access = READ_ONLY)
        String userId,

        @Schema(description = "Email address", example = "john.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "Password for the new account", example = "SecurePass123!")
        @JsonProperty(access = WRITE_ONLY)
        @ValidPassword
        String password,

        @Schema(description = "User's first name", example = "John")
        @NotBlank(message = "First name is required")
        @Size(max = 50, message = "First name must not exceed 50 characters")
        String firstname,

        @Schema(description = "User's last name", example = "Doe")
        @NotBlank(message = "Last name is required")
        @Size(max = 50, message = "Last name must not exceed 50 characters")
        String lastname
) {
}