package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.exception.UserRegistrationException;
import cz.mp.building_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.building_diary.mapper.UserMapper;
import cz.mp.building_diary.repository.UserRepository;
import cz.mp.building_diary.service.KeycloakService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static cz.mp.building_diary.util.TestFixtures.EMAIL;
import static cz.mp.building_diary.util.TestFixtures.KEYCLOAK_ID;
import static cz.mp.building_diary.util.TestFixtures.PASSWORD;
import static cz.mp.building_diary.util.TestFixtures.keycloakUser;
import static cz.mp.building_diary.util.TestFixtures.registrationRequest;
import static cz.mp.building_diary.util.TestFixtures.registrationResponse;
import static cz.mp.building_diary.util.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    class RegisterUser {

        @Test
        void shouldRegisterUserSuccessfully() {
            var request = registrationRequest();
            var kcUser = keycloakUser();
            var entity = user();
            var response = registrationResponse();

            when(userRepository.existsByEmail(EMAIL)).thenReturn(false);
            when(userMapper.toKeycloakUser(request)).thenReturn(kcUser);
            when(keycloakService.createUser(kcUser, PASSWORD)).thenReturn(KEYCLOAK_ID);
            when(userMapper.toEntity(request, KEYCLOAK_ID)).thenReturn(entity);
            when(userRepository.save(entity)).thenReturn(entity);
            when(userMapper.toResponseDto(entity)).thenReturn(response);

            UserRegistrationResponseDto result = userService.registerUser(request);

            assertThat(result).isNotNull();
            assertThat(result.userId()).isEqualTo(KEYCLOAK_ID);
            assertThat(result.email()).isEqualTo(EMAIL);

            verify(userRepository).existsByEmail(EMAIL);
            verify(keycloakService).createUser(kcUser, PASSWORD);
            verify(userRepository).save(entity);
        }

        @Test
        void shouldThrowExceptionWhenEmailExistsInDatabase() {
            when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> userService.registerUser(registrationRequest()))
                    .isInstanceOf(UserRegistrationException.class);

            verify(keycloakService, never()).createUser(any(), any());
            verify(userRepository, never()).save(any());
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
        }
    }
}