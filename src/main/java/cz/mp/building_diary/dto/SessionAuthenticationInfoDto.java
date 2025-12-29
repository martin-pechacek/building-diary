package cz.mp.building_diary.dto;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

public record SessionAuthenticationInfoDto(
        String username,
        String email,
        List<String> roles,
        Instant accessTokenExpiration,
        Instant refreshTokenExpiration,
        String accessToken,
        String refreshToken
) implements Serializable {

    public boolean isAccessTokenExpired() {
        return Instant.now().isAfter(accessTokenExpiration);
    }

    public boolean isRefreshTokenExpired() {
        return Instant.now().isAfter(refreshTokenExpiration);
    }

    public SessionAuthenticationInfoDto withNewTokens(
            String newAccessToken,
            String newRefreshToken,
            Instant newAccessTokenExpiration,
            Instant newRefreshTokenExpiration
    ) {
        return new SessionAuthenticationInfoDto(
                username,
                email,
                roles,
                newAccessTokenExpiration,
                newRefreshTokenExpiration,
                newAccessToken,
                newRefreshToken
        );
    }
}