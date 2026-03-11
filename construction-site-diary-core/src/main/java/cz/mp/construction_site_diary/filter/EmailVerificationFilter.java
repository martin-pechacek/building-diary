package cz.mp.construction_site_diary.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mp.construction_site_diary.controller.AuthController;
import cz.mp.construction_site_diary.dto.ErrorDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        boolean blocked = !HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())
                && isAuthenticatedUser()
                && !isEmailVerified();

        if (blocked) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getWriter(),
                    new ErrorDto("Email verification required to perform this action", HttpStatus.FORBIDDEN.value()));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticatedUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(auth -> !auth.getPrincipal().equals("anonymousUser"))
                .orElse(false);
    }

    private boolean isEmailVerified() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getDetails)
                .map(Boolean.TRUE::equals)
                .orElse(false);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().contains(AuthController.URL);
    }
}