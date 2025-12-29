package cz.mp.building_diary.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mp.building_diary.dto.LoginRequestDto;
import cz.mp.building_diary.dto.LoginResponseDto;
import cz.mp.building_diary.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.dto.UserRegistrationResponseDto;
import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.exception.AuthenticationException;
import cz.mp.building_diary.exception.UserRegistrationException;
import cz.mp.building_diary.exception.UserRegistrationException.ErrorCode;
import cz.mp.building_diary.mapper.UserMapper;
import cz.mp.building_diary.repository.UserRepository;
import cz.mp.building_diary.service.KeycloakService;
import cz.mp.building_diary.service.UserService;
import cz.mp.building_diary.dto.KeycloakTokenDto;
import cz.mp.building_diary.dto.SessionAuthenticationInfoDto;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);
    public static final String SESSION_AUTH_INFO = "auth_info";

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public UserRegistrationResponseDto registerUser(UserRegistrationRequestDto request) {
        validateEmailAvailable(request.email());

        String keycloakId = keycloakService.createUser(
                userMapper.toKeycloakUser(request),
                request.password()
        );

        User user = userMapper.toEntity(request, keycloakId);
        userRepository.save(user);

        LOG.info("User registered successfully: {}", request.email());

        return userMapper.toResponseDto(user);
    }

    private void validateEmailAvailable(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new UserRegistrationException("Email already exists", ErrorCode.EMAIL_EXISTS);
        }
    }

    @Override
    public LoginResponseDto login(LoginRequestDto request, HttpSession session) {
        KeycloakTokenDto tokenResponse = keycloakService.authenticate(request.email(), request.password());

        SessionAuthenticationInfoDto authInfo = createAuthenticationInfo(
                tokenResponse,
                request.email()
        );
        session.setAttribute(SESSION_AUTH_INFO, authInfo);

        LOG.info("User logged in successfully: {}", authInfo.email());

        return new LoginResponseDto(authInfo.email(), authInfo.roles());
    }

    @Override
    public LoginResponseDto refreshSession(HttpSession session) {
        SessionAuthenticationInfoDto currentAuth = (SessionAuthenticationInfoDto) session.getAttribute(SESSION_AUTH_INFO);
        if (currentAuth == null) {
            throw new AuthenticationException("No active session", AuthenticationException.ErrorCode.INVALID_CREDENTIALS);
        }

        if (currentAuth.isRefreshTokenExpired()) {
            session.invalidate();
            throw new AuthenticationException("Session expired, please login again", AuthenticationException.ErrorCode.SESSION_EXPIRED);
        }

        KeycloakTokenDto tokenResponse = keycloakService.refreshToken(currentAuth.refreshToken());

        Instant accessTokenExpiration = Instant.now().plusSeconds(tokenResponse.expiresIn());
        Instant refreshTokenExpiration = Instant.now().plusSeconds(tokenResponse.refreshExpiresIn());

        SessionAuthenticationInfoDto newAuthInfo = currentAuth.withNewTokens(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                accessTokenExpiration,
                refreshTokenExpiration
        );
        session.setAttribute(SESSION_AUTH_INFO, newAuthInfo);

        LOG.debug("Session refreshed for user: {}", newAuthInfo.username());

        return new LoginResponseDto(newAuthInfo.username(), newAuthInfo.roles());
    }

    private SessionAuthenticationInfoDto createAuthenticationInfo(KeycloakTokenDto tokenResponse, String email) {
        Map<String, Object> claims = extractAllClaims(tokenResponse.accessToken());

        String username = (String) claims.get("preferred_username");
        List<String> roles = extractRolesFromClaims(claims);
        Instant accessTokenExpiration = Instant.now().plusSeconds(tokenResponse.expiresIn());
        Instant refreshTokenExpiration = Instant.now().plusSeconds(tokenResponse.refreshExpiresIn());

        return new SessionAuthenticationInfoDto(
                username,
                email,
                roles,
                accessTokenExpiration,
                refreshTokenExpiration,
                tokenResponse.accessToken(),
                tokenResponse.refreshToken()
        );
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

    @SuppressWarnings("unchecked")
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