package cz.mp.construction_site_diary.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mp.construction_site_diary.dto.KeycloakTokenDto;
import cz.mp.construction_site_diary.dto.LoginDto;
import cz.mp.construction_site_diary.service.KeycloakService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Nested
    class Login {

        @Test
        void shouldReturnLoginDtoWithTokensAndRoles() {
            authenticationService = new AuthenticationServiceImpl(keycloakService, new ObjectMapper());
            String token = buildToken("""
                    {"realm_access":{"roles":["user","offline_access"]}}""");
            when(keycloakService.authenticate("user@test.com", "secret"))
                    .thenReturn(new KeycloakTokenDto(token, "refresh-token", 300, 1800, "Bearer", "openid"));

            LoginDto result = authenticationService.login(new LoginDto("user@test.com", "secret", null, null, null));

            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.accessToken()).isEqualTo(token);
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.roles()).containsExactlyInAnyOrder("user", "offline_access");
            assertThat(result.password()).isNull();
        }

        @Test
        void shouldReturnEmptyRolesWhenNoRealmAccess() {
            authenticationService = new AuthenticationServiceImpl(keycloakService, new ObjectMapper());
            String token = buildToken("""
                    {"preferred_username":"user@test.com"}""");
            when(keycloakService.authenticate("user@test.com", "secret"))
                    .thenReturn(new KeycloakTokenDto(token, "refresh-token", 300, 1800, "Bearer", "openid"));

            LoginDto result = authenticationService.login(new LoginDto("user@test.com", "secret", null, null, null));

            assertThat(result.roles()).isEmpty();
        }
    }

    @Nested
    class Refresh {

        @Test
        void shouldReturnLoginDtoWithEmailAndRolesFromNewToken() {
            authenticationService = new AuthenticationServiceImpl(keycloakService, new ObjectMapper());
            String newToken = buildToken("""
                    {"email":"user@test.com","realm_access":{"roles":["user"]}}""");
            when(keycloakService.refreshToken("old-refresh-token"))
                    .thenReturn(new KeycloakTokenDto(newToken, "new-refresh-token", 300, 1800, "Bearer", "openid"));

            LoginDto result = authenticationService.refresh("old-refresh-token");

            assertThat(result.email()).isEqualTo("user@test.com");
            assertThat(result.accessToken()).isEqualTo(newToken);
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            assertThat(result.roles()).containsExactly("user");
        }

        @Test
        void shouldReturnNullEmailWhenClaimAbsent() {
            authenticationService = new AuthenticationServiceImpl(keycloakService, new ObjectMapper());
            String newToken = buildToken("""
                    {"realm_access":{"roles":[]}}""");
            when(keycloakService.refreshToken("refresh-token"))
                    .thenReturn(new KeycloakTokenDto(newToken, "new-refresh", 300, 1800, "Bearer", "openid"));

            LoginDto result = authenticationService.refresh("refresh-token");

            assertThat(result.email()).isNull();
        }
    }

    private String buildToken(String payloadJson) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
        return header + "." + payload + ".fakesignature";
    }
}
