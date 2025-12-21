package cz.mp.building_diary.controller.v1;

import cz.mp.building_diary.controller.v1.dto.ErrorDto;
import cz.mp.building_diary.controller.v1.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.controller.v1.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthController.URL)
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController extends BaseController {

    public static final String URL = BASE_PATH + "/auth";

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account in Keycloak with the default USER role"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserRegistrationResponseDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Keycloak service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            )
    })
    public UserRegistrationResponseDto register(@Valid @RequestBody UserRegistrationRequestDto request) {
        return userService.registerUser(request);
    }
}