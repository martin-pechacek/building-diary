package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.dto.UserRegistrationDto;
import cz.mp.construction_site_diary.enums.TokenType;
import cz.mp.construction_site_diary.event.EmailVerificationEvent;
import cz.mp.construction_site_diary.exception.UserRegistrationException;
import cz.mp.construction_site_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.construction_site_diary.mapper.UserMapper;
import cz.mp.construction_site_diary.repository.UserRepository;
import cz.mp.construction_site_diary.service.EmailVerificationService;
import cz.mp.construction_site_diary.service.KeycloakService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static cz.mp.construction_site_diary.util.TestFixtures.EMAIL;
import static cz.mp.construction_site_diary.util.TestFixtures.KEYCLOAK_ID;
import static cz.mp.construction_site_diary.util.TestFixtures.PASSWORD;
import static cz.mp.construction_site_diary.util.TestFixtures.keycloakUser;
import static cz.mp.construction_site_diary.util.TestFixtures.registrationRequest;
import static cz.mp.construction_site_diary.util.TestFixtures.registrationResponse;
import static cz.mp.construction_site_diary.util.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "baseUrl", "http://localhost:8080");
    }

    @Nested
    class RegisterUser {

        @Test
        void shouldRegisterUserSuccessfully() {
            var request = registrationRequest();
            var kcUser = keycloakUser();
            var entity = user();
            entity.setId(java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
            var response = registrationResponse();
            String verificationToken = "test-verification-token";

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(userMapper.toKeycloakUser(request)).thenReturn(kcUser);
            when(keycloakService.createUser(kcUser, PASSWORD)).thenReturn(KEYCLOAK_ID);
            when(userMapper.toEntity(request, KEYCLOAK_ID)).thenReturn(entity);
            when(userRepository.save(entity)).thenReturn(entity);
            when(userMapper.toDto(entity)).thenReturn(response);
            when(emailVerificationService.createToken(entity, TokenType.EMAIL_VERIFICATION)).thenReturn(verificationToken);

            UserRegistrationDto result = userService.registerUser(request);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(KEYCLOAK_ID);
            assertThat(result.email()).isEqualTo(EMAIL);

            verify(userRepository).existsByEmail(EMAIL);
            verify(keycloakService).createUser(kcUser, PASSWORD);
            verify(userRepository).save(entity);
            verify(emailVerificationService).createToken(entity, TokenType.EMAIL_VERIFICATION);

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue()).isInstanceOf(EmailVerificationEvent.class);
            EmailVerificationEvent publishedEvent = (EmailVerificationEvent) eventCaptor.getValue();
            assertThat(publishedEvent.email()).isEqualTo(EMAIL);
            assertThat(publishedEvent.verificationUrl()).contains(verificationToken);
        }

        @Test
        void shouldThrowExceptionWhenEmailExistsInDatabase() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> userService.registerUser(registrationRequest()))
                    .isInstanceOf(UserRegistrationException.class);

            verify(keycloakService, never()).createUser(any(), any());
            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void shouldPropagateKeycloakException() {
            var request = registrationRequest();
            var kcUser = keycloakUser();

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(userMapper.toKeycloakUser(request)).thenReturn(kcUser);
            when(keycloakService.createUser(kcUser, PASSWORD))
                    .thenThrow(new UserRegistrationException("Email already exists in Keycloak", ErrorCode.EMAIL_EXISTS));

            assertThatThrownBy(() -> userService.registerUser(request))
                    .isInstanceOf(UserRegistrationException.class);

            verify(userRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        void shouldBuildVerificationUrlFromBaseUrlAndToken() {
            var request = registrationRequest();
            var kcUser = keycloakUser();
            var entity = user();
            entity.setId(java.util.UUID.fromString("123e4567-e89b-12d3-a456-426614174001"));
            String token = "abc-123";

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(userMapper.toKeycloakUser(request)).thenReturn(kcUser);
            when(keycloakService.createUser(kcUser, PASSWORD)).thenReturn(KEYCLOAK_ID);
            when(userMapper.toEntity(request, KEYCLOAK_ID)).thenReturn(entity);
            when(userRepository.save(entity)).thenReturn(entity);
            when(userMapper.toDto(entity)).thenReturn(registrationResponse());
            when(emailVerificationService.createToken(eq(entity), eq(TokenType.EMAIL_VERIFICATION))).thenReturn(token);

            userService.registerUser(request);

            ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue()).isInstanceOf(EmailVerificationEvent.class);
            EmailVerificationEvent publishedEvent = (EmailVerificationEvent) eventCaptor.getValue();
            assertThat(publishedEvent.verificationUrl())
                    .isEqualTo("http://localhost:8080/api/v1/auth/verify-email?token=" + token);
        }
    }
}