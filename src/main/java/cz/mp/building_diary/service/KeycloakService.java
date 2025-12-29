package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.KeycloakTokenDto;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakService {

    String createUser(UserRepresentation user, String password);

    void deleteUser(String userId);

    KeycloakTokenDto authenticate(String username, String password);

    KeycloakTokenDto refreshToken(String refreshToken);
}