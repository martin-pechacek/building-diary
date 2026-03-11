package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.exception.UserNotFoundException;
import cz.mp.construction_site_diary.repository.UserRepository;
import cz.mp.construction_site_diary.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";

    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getName)
                .flatMap(email -> userRepository.findByEmail(email))
                .orElseThrow(() -> new UserNotFoundException("No authenticated user or user not found"));
    }

    @Override
    public boolean isAdmin() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getAuthorities)
                .map(authorities -> authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .anyMatch(ROLE_ADMIN::equals))
                .orElse(false);
    }

    @Override
    public boolean isEmailVerified() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getDetails)
                .map(details -> Boolean.TRUE.equals(details))
                .orElse(false);
    }
}