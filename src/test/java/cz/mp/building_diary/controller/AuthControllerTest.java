package cz.mp.building_diary.controller;

import cz.mp.building_diary.dto.LoginDto;
import cz.mp.building_diary.dto.UserRegistrationDto;
import cz.mp.building_diary.exception.AuthenticationException;
import cz.mp.building_diary.exception.GlobalExceptionHandler;
import cz.mp.building_diary.exception.UserRegistrationException;
import cz.mp.building_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.building_diary.facade.AuthFacade;
import cz.mp.building_diary.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static cz.mp.building_diary.controller.AuthController.URL;
import static cz.mp.building_diary.util.JsonTestUtil.toJson;
import static cz.mp.building_diary.util.TestFixtures.EMAIL;
import static cz.mp.building_diary.util.TestFixtures.KEYCLOAK_ID;
import static cz.mp.building_diary.util.TestFixtures.loginRequest;
import static cz.mp.building_diary.util.TestFixtures.loginResponse;
import static cz.mp.building_diary.util.TestFixtures.registrationRequest;
import static cz.mp.building_diary.util.TestFixtures.registrationResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private static final String REGISTER_URL = URL + "/register";
    private static final String LOGIN_URL = URL + "/login";

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private AuthFacade authFacade;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    class Register {

        @Test
        void shouldRegisterUserSuccessfully() throws Exception {
            when(userService.registerUser(any())).thenReturn(registrationResponse());

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(registrationRequest())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.accessToken").value(any()));
        }

        @Test
        void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
            UserRegistrationDto request = new UserRegistrationDto(null, "", "SecurePass123!", "John", "Doe");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any());
        }

        @Test
        void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
            UserRegistrationDto request = new UserRegistrationDto(null, "invalid-email", "SecurePass123!", "John", "Doe");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any());
        }

        @Test
        void shouldReturnBadRequestWhenPasswordTooShort() throws Exception {
            UserRegistrationDto request = new UserRegistrationDto(null, EMAIL, "short", "John", "Doe");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any());
        }

        @Test
        void shouldReturnConflictWhenEmailAlreadyExists() throws Exception {
            when(userService.registerUser(any()))
                    .thenThrow(new UserRegistrationException("Email already exists", ErrorCode.EMAIL_EXISTS));

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(registrationRequest())))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email already exists"));
        }

        @Test
        void shouldReturnServiceUnavailableWhenKeycloakFails() throws Exception {
            when(userService.registerUser(any()))
                    .thenThrow(new UserRegistrationException("Keycloak unavailable", ErrorCode.KEYCLOAK_ERROR));

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(registrationRequest())))
                    .andExpect(status().isServiceUnavailable());
        }

        @Test
        void shouldReturnBadRequestWhenFirstNameIsBlank() throws Exception {
            UserRegistrationDto request = new UserRegistrationDto(null, EMAIL, "SecurePass123!", "", "Doe");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any());
        }

        @Test
        void shouldReturnBadRequestWhenLastNameIsBlank() throws Exception {
            UserRegistrationDto request = new UserRegistrationDto(null, EMAIL, "SecurePass123!", "John", "");

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).registerUser(any());
        }
    }

    @Nested
    class Login {

        @Test
        void shouldLoginSuccessfully() throws Exception {
            when(authFacade.login(any(LoginDto.class))).thenReturn(loginResponse());

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest())))
                    .andExpect(status().isOk());
        }

        @Test
        void shouldReturnBadRequestWhenEmailIsBlank() throws Exception {
            LoginDto request = new LoginDto("", "SecurePass123!", null, null, null);

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(authFacade, never()).login(any());
        }

        @Test
        void shouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
            LoginDto request = new LoginDto("invalid-email", "SecurePass123!", null, null, null);

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(authFacade, never()).login(any());
        }

        @Test
        void shouldReturnBadRequestWhenPasswordIsBlank() throws Exception {
            LoginDto request = new LoginDto(EMAIL, "", null, null, null);

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(request)))
                    .andExpect(status().isBadRequest());

            verify(authFacade, never()).login(any());
        }

        @Test
        void shouldReturnUnauthorizedWhenCredentialsAreInvalid() throws Exception {
            when(authFacade.login(any()))
                    .thenThrow(new AuthenticationException("Invalid credentials", AuthenticationException.ErrorCode.INVALID_CREDENTIALS));

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest())))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        @Test
        void shouldReturnServiceUnavailableWhenKeycloakFails() throws Exception {
            when(authFacade.login(any()))
                    .thenThrow(new AuthenticationException("Keycloak unavailable", AuthenticationException.ErrorCode.KEYCLOAK_ERROR));

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest())))
                    .andExpect(status().isServiceUnavailable());
        }
    }
}