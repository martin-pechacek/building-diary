package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.dto.KeycloakTokenDto;
import cz.mp.construction_site_diary.exception.AuthenticationException;
import cz.mp.construction_site_diary.exception.UserRegistrationException;
import cz.mp.construction_site_diary.properties.KeycloakAdminProperties;
import cz.mp.construction_site_diary.properties.KeycloakClientProperties;
import cz.mp.construction_site_diary.service.KeycloakService;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakServiceImpl implements KeycloakService {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakServiceImpl.class);
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    private final Keycloak keycloak;
    private final KeycloakAdminProperties adminProperties;
    private final KeycloakClientProperties clientProperties;
    private final RestClient restClient = RestClient.create();

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
            throw new UserRegistrationException("Email already exists in Keycloak", UserRegistrationException.ErrorCode.EMAIL_EXISTS);
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
        String roleName = adminProperties.defaultRole();
        RealmResource realmResource = getRealmResource();

        RoleRepresentation role = realmResource.roles().get(roleName).toRepresentation();
        if (role == null) {
            throw new UserRegistrationException("Default role not found: " + roleName, UserRegistrationException.ErrorCode.ROLE_NOT_FOUND);
        }

        realmResource.users().get(userId).roles().realmLevel().add(List.of(role));
        LOG.debug("Assigned role {} to user {}", roleName, userId);
    }

    private RealmResource getRealmResource() {
        return keycloak.realm(adminProperties.targetRealm());
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
            throw new UserRegistrationException("Failed to create user: " + errorMessage, UserRegistrationException.ErrorCode.KEYCLOAK_ERROR);
        } catch (UserRegistrationException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Error communicating with Keycloak", e);
            throw new UserRegistrationException("Failed to communicate with Keycloak", UserRegistrationException.ErrorCode.KEYCLOAK_ERROR, e);
        }
    }

    @Override
    public KeycloakTokenDto authenticate(String username, String password) {
        var request = createPasswordGrantRequest(username, password);

        try {
            return restClient
                    .post()
                    .uri(clientProperties.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(request)
                    .retrieve()
                    .body(KeycloakTokenDto.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            LOG.debug("Invalid credentials for user: {}", username);
            throw new AuthenticationException("Invalid credentials", AuthenticationException.ErrorCode.INVALID_CREDENTIALS);
        } catch (Exception e) {
            LOG.error("Error authenticating with Keycloak", e);
            throw new AuthenticationException("Failed to communicate with Keycloak", AuthenticationException.ErrorCode.KEYCLOAK_ERROR, e);
        }
    }

    @Override
    public KeycloakTokenDto refreshToken(String refreshToken) {
        var request = createRefreshTokenRequest(refreshToken);

        try {
            return restClient
                    .post()
                    .uri(clientProperties.tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(request)
                    .retrieve()
                    .body(KeycloakTokenDto.class);
        } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
            LOG.debug("Invalid or expired refresh token");
            throw new AuthenticationException("Session expired", AuthenticationException.ErrorCode.SESSION_EXPIRED);
        } catch (Exception e) {
            LOG.error("Error refreshing token with Keycloak", e);
            throw new AuthenticationException("Failed to communicate with Keycloak", AuthenticationException.ErrorCode.KEYCLOAK_ERROR, e);
        }
    }

    private MultiValueMap<String, String> createPasswordGrantRequest(String username, String password) {
        var request = createBaseTokenRequest();
        request.add("grant_type", GRANT_TYPE_PASSWORD);
        request.add("username", username);
        request.add("password", password);
        return request;
    }

    private MultiValueMap<String, String> createRefreshTokenRequest(String refreshToken) {
        var request = createBaseTokenRequest();
        request.add("grant_type", GRANT_TYPE_REFRESH_TOKEN);
        request.add("refresh_token", refreshToken);
        return request;
    }

    private MultiValueMap<String, String> createBaseTokenRequest() {
        var request = new LinkedMultiValueMap<String, String>();
        request.add("client_id", clientProperties.clientId());
        request.add("client_secret", clientProperties.clientSecret());
        return request;
    }
}