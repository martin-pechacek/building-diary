package cz.mp.building_diary.config;

import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestSecurityConfig {

    @Bean
    @Primary
    public Keycloak keycloakAdmin() {
        return mock(Keycloak.class);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        ClientRegistration registration = ClientRegistration.withRegistrationId("keycloak")
                .clientId("test-client")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .scope("openid", "profile", "email")
                .authorizationUri("http://localhost:8180/realms/test/protocol/openid-connect/auth")
                .tokenUri("http://localhost:8180/realms/test/protocol/openid-connect/token")
                .userInfoUri("http://localhost:8180/realms/test/protocol/openid-connect/userinfo")
                .jwkSetUri("http://localhost:8180/realms/test/protocol/openid-connect/certs")
                .userNameAttributeName("preferred_username")
                .build();

        return new InMemoryClientRegistrationRepository(registration);
    }
}