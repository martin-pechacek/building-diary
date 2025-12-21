package cz.mp.building_diary.util;

import cz.mp.building_diary.controller.v1.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.controller.v1.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.entity.User;
import org.keycloak.representations.idm.UserRepresentation;

public final class TestFixtures {

    public static final String EMAIL = "john.doe@example.com";
    public static final String PASSWORD = "SecurePass123!";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";
    public static final String KEYCLOAK_ID = "kc-123-456";

    private TestFixtures() {
    }

    public static UserRegistrationRequestDto registrationRequest() {
        return new UserRegistrationRequestDto(EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
    }

    public static UserRegistrationResponseDto registrationResponse() {
        return new UserRegistrationResponseDto(KEYCLOAK_ID, EMAIL);
    }

    public static User user() {
        return new User(KEYCLOAK_ID, EMAIL, FIRST_NAME, LAST_NAME);
    }

    public static UserRepresentation keycloakUser() {
        UserRepresentation user = new UserRepresentation();
        user.setEmail(EMAIL);
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        return user;
    }
}
