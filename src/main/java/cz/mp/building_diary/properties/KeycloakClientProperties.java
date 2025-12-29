package cz.mp.building_diary.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "keycloak.client")
public record KeycloakClientProperties(
        String serverUrl,
        String realm,
        String clientId,
        String clientSecret
) {

    public String tokenUrl() {
        return serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }
}