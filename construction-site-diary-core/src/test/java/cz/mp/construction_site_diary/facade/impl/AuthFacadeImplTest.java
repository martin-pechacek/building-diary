package cz.mp.construction_site_diary.facade.impl;

import cz.mp.construction_site_diary.dto.LoginDto;
import cz.mp.construction_site_diary.dto.UserRegistrationDto;
import cz.mp.construction_site_diary.service.AuthenticationService;
import cz.mp.construction_site_diary.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFacadeImplTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthFacadeImpl authFacade;

    @Nested
    class Register {

        @Test
        void shouldRegisterAndLoginUser() {
            UserRegistrationDto registrationDto = new UserRegistrationDto(
                    null, "test@example.com", "Password123!", "John", "Doe"
            );
            UserRegistrationDto registeredUser = new UserRegistrationDto(
                    "kc-123", "test@example.com", null, "John", "Doe"
            );
            LoginDto loginResponse = new LoginDto(
                    "test@example.com", null, "access-token", "refresh-token", List.of("USER")
            );

            when(userService.registerUser(registrationDto)).thenReturn(registeredUser);
            when(authenticationService.login(any(LoginDto.class))).thenReturn(loginResponse);

            LoginDto result = authFacade.register(registrationDto);

            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            assertThat(result.roles()).containsExactly("USER");
            verify(userService).registerUser(registrationDto);
            verify(authenticationService).login(any(LoginDto.class));
        }
    }

    @Nested
    class Login {

        @Test
        void shouldLoginUser() {
            LoginDto loginRequest = new LoginDto("test@example.com", "password", null, null, null);
            LoginDto loginResponse = new LoginDto(
                    "test@example.com", null, "access-token", "refresh-token", List.of("USER")
            );

            when(authenticationService.login(loginRequest)).thenReturn(loginResponse);

            LoginDto result = authFacade.login(loginRequest);

            assertThat(result.accessToken()).isEqualTo("access-token");
            assertThat(result.refreshToken()).isEqualTo("refresh-token");
            verify(authenticationService).login(loginRequest);
        }
    }

    @Nested
    class Refresh {

        @Test
        void shouldRefreshTokens() {
            String refreshToken = "old-refresh-token";
            LoginDto refreshResponse = new LoginDto(
                    "test@example.com", null, "new-access-token", "new-refresh-token", List.of("USER")
            );

            when(authenticationService.refresh(refreshToken)).thenReturn(refreshResponse);

            LoginDto result = authFacade.refresh(refreshToken);

            assertThat(result.accessToken()).isEqualTo("new-access-token");
            assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
            verify(authenticationService).refresh(refreshToken);
        }
    }
}