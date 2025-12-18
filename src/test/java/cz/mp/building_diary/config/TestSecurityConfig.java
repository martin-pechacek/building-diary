package cz.mp.building_diary.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

@TestConfiguration
public class TestSecurityConfig {

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