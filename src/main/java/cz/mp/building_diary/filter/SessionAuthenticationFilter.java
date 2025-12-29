package cz.mp.building_diary.filter;

import cz.mp.building_diary.dto.SessionAuthenticationInfoDto;
import cz.mp.building_diary.service.impl.UserServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(SessionAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session != null) {
            SessionAuthenticationInfoDto authInfo = (SessionAuthenticationInfoDto) session.getAttribute(UserServiceImpl.SESSION_AUTH_INFO);
            if (authInfo != null) {
                if (!authInfo.isAccessTokenExpired()) {
                    setSecurityContext(authInfo);
                } else {
                    LOG.debug("Access token expired for user: {}", authInfo.username());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void setSecurityContext(SessionAuthenticationInfoDto authInfo) {
        List<SimpleGrantedAuthority> authorities = authInfo.roles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                .toList();

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                authInfo.username(),
                null,
                authorities
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        LOG.debug("Set security context for user: {} with roles: {}", authInfo.username(), authInfo.roles());
    }
}