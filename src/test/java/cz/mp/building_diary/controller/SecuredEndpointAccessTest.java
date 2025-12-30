package cz.mp.building_diary.controller;

import cz.mp.building_diary.config.SecurityConfig;
import cz.mp.building_diary.config.TestSecurityConfig;
import cz.mp.building_diary.service.AuthenticationService;
import cz.mp.building_diary.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static cz.mp.building_diary.util.JsonTestUtil.toJson;
import static cz.mp.building_diary.util.TestFixtures.loginRequest;
import static cz.mp.building_diary.util.TestFixtures.loginResponse;
import static cz.mp.building_diary.util.TestFixtures.registrationRequest;
import static cz.mp.building_diary.util.TestFixtures.registrationResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, TestSecurityConfig.class})
@DisplayName("Secured Endpoint Access")
class SecuredEndpointAccessTest {

    private static final String REGISTER_URL = AuthController.URL + "/register";
    private static final String LOGIN_URL = AuthController.URL + "/login";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Nested
    @DisplayName("Public Endpoints - No Authentication Required")
    class PublicEndpoints {

        @Test
        @DisplayName("should allow access to /auth/register without authentication")
        void shouldAllowAccessToRegisterWithoutAuthentication() throws Exception {
            when(userService.registerUser(any())).thenReturn(registrationResponse());

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(registrationRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("should allow access to /auth/login without authentication")
        void shouldAllowAccessToLoginWithoutAuthentication() throws Exception {
            when(authenticationService.login(any(), any())).thenReturn(loginResponse());

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest())))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Public Endpoints - With Authentication")
    class PublicEndpointsWithAuth {

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should allow access to /auth/register even when authenticated")
        void shouldAllowAccessToRegisterWhenAuthenticated() throws Exception {
            when(userService.registerUser(any())).thenReturn(registrationResponse());

            mockMvc.perform(post(REGISTER_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(registrationRequest())))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should allow access to /auth/login even when authenticated")
        void shouldAllowAccessToLoginWhenAuthenticated() throws Exception {
            when(authenticationService.login(any(), any())).thenReturn(loginResponse());

            mockMvc.perform(post(LOGIN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(toJson(loginRequest())))
                    .andExpect(status().isOk());
        }
    }
}