package cz.mp.construction_site_diary.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class EmailVerificationFilterTest {

    private final EmailVerificationFilter filter = new EmailVerificationFilter(new ObjectMapper().findAndRegisterModules());

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class Blocks {

        @Test
        void shouldReturn403ForNonGetRequestWithUnverifiedUser() throws Exception {
            setAuthentication("user@test.com", false);
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/projects");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
            assertThat(response.getContentType()).contains("application/json");
            verify(chain, never()).doFilter(request, response);
        }
    }

    @Nested
    class Passes {

        @Test
        void shouldPassGetRequestForUnverifiedUser() throws Exception {
            setAuthentication("user@test.com", false);
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/projects");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        void shouldPassNonGetRequestForVerifiedUser() throws Exception {
            setAuthentication("user@test.com", true);
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/projects");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        void shouldPassNonGetRequestForUnauthenticatedUser() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/projects");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }

        @Test
        void shouldSkipFilterForAuthUrl() throws Exception {
            setAuthentication("user@test.com", false);
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
            MockHttpServletResponse response = new MockHttpServletResponse();
            FilterChain chain = mock(FilterChain.class);

            filter.doFilter(request, response, chain);

            verify(chain).doFilter(request, response);
        }
    }

    private void setAuthentication(String username, boolean emailVerified) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null, List.of());
        auth.setDetails(emailVerified);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
