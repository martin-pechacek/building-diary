package cz.mp.building_diary.controller;

import cz.mp.building_diary.dto.ErrorDto;
import cz.mp.building_diary.dto.LoginDto;
import cz.mp.building_diary.dto.UserRegistrationDto;
import cz.mp.building_diary.service.AuthenticationService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(AuthController.URL)
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController extends BaseController {

    public static final String URL = BASE_PATH + "/auth";

    private final UserService userService;
    private final AuthenticationService authenticationService;

    public AuthController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
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
                    content = @Content(schema = @Schema(implementation = UserRegistrationDto.class))
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
    public UserRegistrationDto register(@Valid @RequestBody UserRegistrationDto dto) {
        return userService.registerUser(dto);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Authenticates user via Keycloak and returns JWT tokens"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Keycloak service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            )
    })
    public LoginDto login(@Valid @RequestBody LoginDto dto) {
        return authenticationService.login(dto);
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Refresh token",
            description = "Refreshes the access token using the refresh token"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(schema = @Schema(implementation = LoginDto.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired refresh token",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Keycloak service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorDto.class))
            )
    })
    public LoginDto refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        return authenticationService.refresh(refreshToken);
    }
}