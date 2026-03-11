package cz.mp.photos_service.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JwtAuthenticationFilterTest {

    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(new ObjectMapper());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class WithValidToken {

        @Test
        void shouldSetSecurityContextWithUsernameAndRoles() throws Exception {
            String token = buildToken("""
                    {"preferred_username":"user@test.com","realm_access":{"roles":["user"]}}""");

            doFilter("Bearer " + token);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getName()).isEqualTo("user@test.com");
            assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        }

        @Test
        void shouldSetMultipleRoles() throws Exception {
            String token = buildToken("""
                    {"preferred_username":"admin@test.com","realm_access":{"roles":["user","admin"]}}""");

            doFilter("Bearer " + token);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth.getAuthorities()).extracting("authority")
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        }

        @Test
        void shouldSetEmptyAuthoritiesWhenNoRoles() throws Exception {
            String token = buildToken("""
                    {"preferred_username":"user@test.com"}""");

            doFilter("Bearer " + token);

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            assertThat(auth).isNotNull();
            assertThat(auth.getAuthorities()).isEmpty();
        }

        @Test
        void shouldNotSetSecurityContextWhenPreferredUsernameAbsent() throws Exception {
            String token = buildToken("""
                    {"sub":"some-id","email":"user@test.com"}""");

            doFilter("Bearer " + token);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Nested
    class WithInvalidToken {

        @Test
        void shouldSkipWhenNoAuthorizationHeader() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/photos");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
            verify(chain).doFilter(request, response);
        }

        @Test
        void shouldSkipWhenHeaderIsNotBearer() throws Exception {
            doFilter("Basic dXNlcjpwYXNz");

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        void shouldSkipWhenTokenHasWrongNumberOfParts() throws Exception {
            doFilter("Bearer only.twoparts");

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }

        @Test
        void shouldSkipWhenPayloadIsNotValidJson() throws Exception {
            String badPayload = Base64.getUrlEncoder().withoutPadding().encodeToString("not-json".getBytes());
            doFilter("Bearer header." + badPayload + ".sig");

            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    private void doFilter(String authorizationHeader) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/photos");
        request.addHeader("Authorization", authorizationHeader);
        filter.doFilter(request, new MockHttpServletResponse(), mock(FilterChain.class));
    }

    private String buildToken(String payloadJson) {
        String header = Base64.getUrlEncoder().withoutPadding().encodeToString("{\"alg\":\"RS256\"}".getBytes());
        String payload = Base64.getUrlEncoder().withoutPadding().encodeToString(payloadJson.getBytes());
        return header + "." + payload + ".fakesignature";
    }
}
