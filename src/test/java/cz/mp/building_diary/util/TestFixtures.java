package cz.mp.building_diary.util;

import cz.mp.building_diary.dto.LoginRequestDto;
import cz.mp.building_diary.dto.LoginResponseDto;
import cz.mp.building_diary.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.dto.KeycloakTokenDto;
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

    public static LoginRequestDto loginRequest() {
        return new LoginRequestDto(EMAIL, PASSWORD);
    }

    public static LoginResponseDto loginResponse() {
        return new LoginResponseDto(EMAIL, USER_ROLES);
    }

    public static KeycloakTokenDto keycloakTokenDto() {
        return new KeycloakTokenDto(ACCESS_TOKEN, REFRESH_TOKEN, 300, 1800, "Bearer", "openid profile email");
    }
}
