package cz.mp.building_diary.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/api/*/auth/register"
                        ).permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/*/auth/register")
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userAuthoritiesMapper(keycloakAuthoritiesMapper())
                        )
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                );

        return http.build();
    }

    @Bean
    public GrantedAuthoritiesMapper keycloakAuthoritiesMapper() {
        return authorities -> authorities.stream()
                .flatMap(authority -> {
                    Stream<GrantedAuthority> extracted = Stream.empty();

                    if (authority instanceof OidcUserAuthority oidc) {
                        extracted = extractRoles(oidc.getIdToken().getClaims()).stream();
                    } else if (authority instanceof OAuth2UserAuthority oauth2) {
                        extracted = extractRoles(oauth2.getAttributes()).stream();
                    }

                    return Stream.concat(extracted, Stream.of(authority));
                })
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRoles(Map<String, Object> claims) {
        return Optional.ofNullable((Map<String, Object>) claims.get("realm_access"))
                .map(realmAccess -> (List<String>) realmAccess.get("roles"))
                .stream()
                .flatMap(List::stream)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .collect(Collectors.toSet());
    }
}
