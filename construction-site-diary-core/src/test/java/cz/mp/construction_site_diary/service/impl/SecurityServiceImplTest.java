package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.exception.UserNotFoundException;
import cz.mp.construction_site_diary.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SecurityServiceImpl securityService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetCurrentUser {

        @Test
        void shouldReturnUserWhenAuthenticated() {
            User user = new User("kc-id", "user@test.com", "Test", "User");
            setAuthentication("user@test.com", false);
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            User result = securityService.getCurrentUser();

            assertThat(result).isEqualTo(user);
        }

        @Test
        void shouldThrowWhenNoAuthentication() {
            assertThatThrownBy(() -> securityService.getCurrentUser())
                    .isInstanceOf(UserNotFoundException.class);
        }

        @Test
        void shouldThrowWhenUserNotFoundInRepository() {
            setAuthentication("unknown@test.com", false);
            when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> securityService.getCurrentUser())
                    .isInstanceOf(UserNotFoundException.class);
        }
    }

    @Nested
    class IsAdmin {

        @Test
        void shouldReturnTrueWhenUserHasAdminRole() {
            setAuthenticationWithRoles("admin@test.com", "ROLE_ADMIN");

            assertThat(securityService.isAdmin()).isTrue();
        }

        @Test
        void shouldReturnFalseWhenUserLacksAdminRole() {
            setAuthenticationWithRoles("user@test.com", "ROLE_USER");

            assertThat(securityService.isAdmin()).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNoAuthentication() {
            assertThat(securityService.isAdmin()).isFalse();
        }
    }

    @Nested
    class IsEmailVerified {

        @Test
        void shouldReturnTrueWhenDetailsIsTrue() {
            setAuthentication("user@test.com", true);

            assertThat(securityService.isEmailVerified()).isTrue();
        }

        @Test
        void shouldReturnFalseWhenDetailsIsFalse() {
            setAuthentication("user@test.com", false);

            assertThat(securityService.isEmailVerified()).isFalse();
        }

        @Test
        void shouldReturnFalseWhenNoAuthentication() {
            assertThat(securityService.isEmailVerified()).isFalse();
        }
    }

    private void setAuthentication(String username, boolean emailVerified) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
        auth.setDetails(emailVerified);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void setAuthenticationWithRoles(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
