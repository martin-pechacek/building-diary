package cz.mp.construction_site_diary.util;

import cz.mp.construction_site_diary.dto.KeycloakTokenDto;
import cz.mp.construction_site_diary.dto.LoginDto;
import cz.mp.construction_site_diary.dto.UserRegistrationDto;
import cz.mp.construction_site_diary.entity.User;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public final class TestFixtures {

    public static final String EMAIL = "john.doe@example.com";
    public static final String PASSWORD = "SecurePass123!";
    public static final String FIRST_NAME = "John";
    public static final String LAST_NAME = "Doe";
    public static final String KEYCLOAK_ID = "kc-123-456";
    public static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJwcmVmZXJyZWRfdXNlcm5hbWUiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJVU0VSIl19fQ.signature";
    public static final String REFRESH_TOKEN = "refresh-token-123";
    public static final List<String> USER_ROLES = List.of("USER");

    private TestFixtures() {
    }

    public static UserRegistrationDto registrationRequest() {
        return new UserRegistrationDto(null, EMAIL, PASSWORD, FIRST_NAME, LAST_NAME);
    }

    public static UserRegistrationDto registrationResponse() {
        return new UserRegistrationDto(KEYCLOAK_ID, EMAIL, null, FIRST_NAME, LAST_NAME);
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

    public static LoginDto loginRequest() {
        return new LoginDto(EMAIL, PASSWORD, null, null, null);
    }

    public static LoginDto loginResponse() {
        return new LoginDto(EMAIL, null, ACCESS_TOKEN, REFRESH_TOKEN, USER_ROLES);
    }

    public static KeycloakTokenDto keycloakTokenDto() {
        return new KeycloakTokenDto(ACCESS_TOKEN, REFRESH_TOKEN, 300, 1800, "Bearer", "openid profile email");
    }
}
