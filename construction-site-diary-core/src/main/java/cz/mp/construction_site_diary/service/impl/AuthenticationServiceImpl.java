package cz.mp.construction_site_diary.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mp.construction_site_diary.dto.KeycloakTokenDto;
import cz.mp.construction_site_diary.dto.LoginDto;
import cz.mp.construction_site_diary.service.AuthenticationService;
import cz.mp.construction_site_diary.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

    private final KeycloakService keycloakService;
    private final ObjectMapper objectMapper;

    @Override
    public LoginDto login(LoginDto dto) {
        KeycloakTokenDto tokenResponse = keycloakService.authenticate(dto.email(), dto.password());
        List<String> roles = extractRolesFromToken(tokenResponse.accessToken());

        LOG.info("User logged in successfully: {}", dto.email());

        return new LoginDto(
                dto.email(),
                null,
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                roles
        );
    }

    @Override
    public LoginDto refresh(String refreshToken) {
        KeycloakTokenDto tokenResponse = keycloakService.refreshToken(refreshToken);
        List<String> roles = extractRolesFromToken(tokenResponse.accessToken());
        String email = extractEmailFromToken(tokenResponse.accessToken());

        LOG.debug("Token refreshed for user: {}", email);

        return new LoginDto(
                email,
                null,
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                roles
        );
    }

    private List<String> extractRolesFromToken(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return extractRolesFromClaims(claims);
    }

    private String extractEmailFromToken(String token) {
        Map<String, Object> claims = extractAllClaims(token);
        return (String) claims.get("email");
    }

    private Map<String, Object> extractAllClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            LOG.error("Failed to extract claims from token", e);
            return Collections.emptyMap();
        }
    }

    private List<String> extractRolesFromClaims(Map<String, Object> claims) {
        Map<String, Object> realmAccess = (Map<String, Object>) claims.get("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                return roles;
            }
        }
        return Collections.emptyList();
    }
}