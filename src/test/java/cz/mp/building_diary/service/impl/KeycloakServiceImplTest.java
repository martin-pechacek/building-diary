package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.exception.UserRegistrationException;
import cz.mp.building_diary.properties.KeycloakAdminProperties;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static cz.mp.building_diary.util.TestFixtures.EMAIL;
import static cz.mp.building_diary.util.TestFixtures.KEYCLOAK_ID;
import static cz.mp.building_diary.util.TestFixtures.PASSWORD;
import static cz.mp.building_diary.util.TestFixtures.keycloakUser;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceImplTest {

    private static final String TARGET_REALM = "building-diary";
    private static final String DEFAULT_ROLE = "USER";

    @Mock
    private Keycloak keycloak;

    @Mock
    private KeycloakAdminProperties properties;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @Mock
    private RolesResource rolesResource;

    @Mock
    private RoleResource roleResource;

    @Mock
    private RoleMappingResource roleMappingResource;

    @Mock
    private RoleScopeResource roleScopeResource;

    @Mock
    private Response response;

    private KeycloakServiceImpl keycloakService;

    @BeforeEach
    void setUp() {
        keycloakService = new KeycloakServiceImpl(keycloak, properties);

        when(properties.targetRealm()).thenReturn(TARGET_REALM);
        when(keycloak.realm(TARGET_REALM)).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
    }

    @Nested
    class CreateUser {

        @Test
        void shouldCreateUserSuccessfully() {
            var user = keycloakUser();

            when(usersResource.searchByEmail(EMAIL, true)).thenReturn(Collections.emptyList());
            when(usersResource.create(user)).thenReturn(response);
            when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(response.getHeaderString("Location")).thenReturn("http://keycloak/users/" + KEYCLOAK_ID);

            when(usersResource.get(KEYCLOAK_ID)).thenReturn(userResource);
            doNothing().when(userResource).resetPassword(any());

            when(properties.defaultRole()).thenReturn(DEFAULT_ROLE);
            when(realmResource.roles()).thenReturn(rolesResource);
            when(rolesResource.get(DEFAULT_ROLE)).thenReturn(roleResource);
            when(roleResource.toRepresentation()).thenReturn(new RoleRepresentation());
            when(userResource.roles()).thenReturn(roleMappingResource);
            when(roleMappingResource.realmLevel()).thenReturn(roleScopeResource);
            doNothing().when(roleScopeResource).add(anyList());

            String result = keycloakService.createUser(user, PASSWORD);

            assertThat(result).isEqualTo(KEYCLOAK_ID);
            verify(usersResource).create(user);
            verify(userResource).resetPassword(any());
        }

        @Test
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            var user = keycloakUser();

            when(usersResource.searchByEmail(EMAIL, true)).thenReturn(List.of(new UserRepresentation()));

            assertThatThrownBy(() -> keycloakService.createUser(user, PASSWORD))
                    .isInstanceOf(UserRegistrationException.class);

            verify(usersResource, never()).create(any());
        }

        @Test
        void shouldThrowExceptionWhenKeycloakReturnsError() {
            var user = keycloakUser();

            when(usersResource.searchByEmail(EMAIL, true)).thenReturn(Collections.emptyList());
            when(usersResource.create(user)).thenReturn(response);
            when(response.getStatus()).thenReturn(Response.Status.BAD_REQUEST.getStatusCode());
            when(response.readEntity(String.class)).thenReturn("Invalid user data");

            assertThatThrownBy(() -> keycloakService.createUser(user, PASSWORD))
                    .isInstanceOf(UserRegistrationException.class);
        }

        @Test
        void shouldDeleteUserWhenRoleAssignmentFails() {
            var user = keycloakUser();

            when(usersResource.searchByEmail(EMAIL, true)).thenReturn(Collections.emptyList());
            when(usersResource.create(user)).thenReturn(response);
            when(response.getStatus()).thenReturn(Response.Status.CREATED.getStatusCode());
            when(response.getHeaderString("Location")).thenReturn("http://keycloak/users/" + KEYCLOAK_ID);

            when(usersResource.get(KEYCLOAK_ID)).thenReturn(userResource);
            doNothing().when(userResource).resetPassword(any());

            when(properties.defaultRole()).thenReturn(DEFAULT_ROLE);
            when(realmResource.roles()).thenReturn(rolesResource);
            when(rolesResource.get(DEFAULT_ROLE)).thenReturn(roleResource);
            when(roleResource.toRepresentation()).thenReturn(null);

            assertThatThrownBy(() -> keycloakService.createUser(user, PASSWORD))
                    .isInstanceOf(UserRegistrationException.class);

            verify(userResource).remove();
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void shouldDeleteUserSuccessfully() {
            when(usersResource.get(KEYCLOAK_ID)).thenReturn(userResource);
            doNothing().when(userResource).remove();

            keycloakService.deleteUser(KEYCLOAK_ID);

            verify(userResource).remove();
        }

        @Test
        void shouldNotThrowWhenDeleteFails() {
            when(usersResource.get(KEYCLOAK_ID)).thenThrow(new RuntimeException("User not found"));

            keycloakService.deleteUser(KEYCLOAK_ID);
        }
    }
}