package cz.mp.construction_site_diary.config;

import cz.mp.construction_site_diary.properties.KeycloakAdminProperties;
import cz.mp.construction_site_diary.properties.KeycloakClientProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({KeycloakAdminProperties.class, KeycloakClientProperties.class})
public class KeycloakAdminConfig {

    private final KeycloakAdminProperties properties;

    public KeycloakAdminConfig(KeycloakAdminProperties properties) {
        this.properties = properties;
    }

    @Bean
    public Keycloak keycloakAdmin() {
        return KeycloakBuilder.builder()
                .serverUrl(properties.serverUrl())
                .realm(properties.realm())
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .grantType("client_credentials")
                .build();
    }
}