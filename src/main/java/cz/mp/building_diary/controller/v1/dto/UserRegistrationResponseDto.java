package cz.mp.building_diary.controller.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User registration response")
public record UserRegistrationResponseDto(
        @Schema(description = "Keycloak user ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String userId,

        @Schema(description = "Registered email", example = "john.doe@example.com")
        String email
) {
}