package cz.mp.building_diary.service;

import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakService {

    String createUser(UserRepresentation user, String password);

    void deleteUser(String userId);
}