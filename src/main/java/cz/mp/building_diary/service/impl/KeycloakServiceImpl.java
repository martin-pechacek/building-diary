package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.exception.UserRegistrationException;
import cz.mp.building_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.building_diary.properties.KeycloakAdminProperties;
import cz.mp.building_diary.service.KeycloakService;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakServiceImpl.class);

    private final Keycloak keycloak;
    private final KeycloakAdminProperties properties;

    @Override
    public String createUser(UserRepresentation user, String password) {
        UsersResource usersResource = getUsersResource();

        validateEmailAvailable(usersResource, user.getEmail());

        String keycloakId = createUserInKeycloak(usersResource, user);

        try {
            setPassword(keycloakId, password);
            assignDefaultRole(keycloakId);
            return keycloakId;
        } catch (Exception e) {
            deleteUser(keycloakId);
            throw e;
        }
    }

    @Override
    public void deleteUser(String userId) {
        try {
            getUsersResource().get(userId).remove();
            LOG.info("Deleted Keycloak user: {}", userId);
        } catch (Exception e) {
            LOG.error("Failed to delete Keycloak user: {}", userId, e);
        }
    }

    private void validateEmailAvailable(UsersResource usersResource, String email) {
        List<UserRepresentation> existingUsers = usersResource.searchByEmail(email, true);
        if (!existingUsers.isEmpty()) {
            throw new UserRegistrationException("Email already exists in Keycloak", ErrorCode.EMAIL_EXISTS);
        }
    }

    private void setPassword(String userId, String password) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);

        getUsersResource().get(userId).resetPassword(credential);
    }

    private void assignDefaultRole(String userId) {
        String roleName = properties.defaultRole();
        RealmResource realmResource = getRealmResource();

        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
        if (role == null) {
            throw new UserRegistrationException("Default role not found: " + roleName, ErrorCode.ROLE_NOT_FOUND);
        }

        realmResource.users().get(userId).roles().realmLevel().add(List.of(role));
        LOG.debug("Assigned role {} to user {}", roleName, userId);
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(properties.targetRealm());
    }

    private UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    private String createUserInKeycloak(UsersResource usersResource, UserRepresentation user) {
        user.setUsername(user.getEmail());
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                String locationHeader = response.getHeaderString("Location");
                return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
            }

            String errorMessage = response.readEntity(String.class);
            LOG.error("Failed to create user in Keycloak: {} - {}", response.getStatus(), errorMessage);
            throw new UserRegistrationException("Failed to create user: " + errorMessage, ErrorCode.KEYCLOAK_ERROR);
        } catch (UserRegistrationException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error communicating with Keycloak", e);
            throw new UserRegistrationException("Failed to communicate with Keycloak", ErrorCode.KEYCLOAK_ERROR, e);
        }
    }
}