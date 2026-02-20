package cz.mp.construction_site_diary.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        try {
            Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                    .filter(header -> header.startsWith(BEARER_PREFIX))
                    .map(header -> header.substring(BEARER_PREFIX.length()))
                    .map(this::extractClaims)
                    .filter(claims -> !claims.isEmpty())
                    .ifPresent(this::setSecurityContext);

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            LOG.debug("Failed to process JWT token: {}", e.getMessage());
        }
    }

    private Map<String, Object> extractClaims(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Collections.emptyMap();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            LOG.debug("Failed to extract claims from token: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
    
    private void setSecurityContext(Map<String, Object> claims) {
        String username = (String) claims.get("preferred_username");
        if (username == null) {
            return; 
        }

        List<String> roles = extractRoles(claims);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        LOG.debug("Set security context for user: {} with roles: {}", username, roles);
    }
    
    private List<String> extractRoles(Map<String, Object> claims) {
        return Optional.ofNullable((Map<String, Object>) claims.get("realm_access"))
                .map(realmAccess -> (List<String>) realmAccess.get("roles"))
                .orElse(Collections.emptyList());
    }
}