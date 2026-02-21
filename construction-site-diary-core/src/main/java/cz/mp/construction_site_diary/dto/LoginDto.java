package cz.mp.construction_site_diary.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Schema(description = "User login")
public record LoginDto(
        @Schema(description = "Email address", example = "john.doe@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @Schema(description = "User password", example = "SecurePass123!")
        @JsonProperty(access = WRITE_ONLY)
        @NotBlank(message = "Password is required")
        String password,

        @Schema(description = "JWT access token")
        @JsonProperty(access = READ_ONLY)
        String accessToken,

        @Schema(description = "JWT refresh token")
        @JsonProperty(access = READ_ONLY)
        String refreshToken,

        @Schema(description = "User roles")
        @JsonProperty(access = READ_ONLY)
        List<String> roles
) {
}