package cz.mp.building_diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User login response")
public record LoginResponseDto(
        @Schema(description = "Email")
        String email,

        @Schema(description = "User roles")
        List<String> roles
) {
}