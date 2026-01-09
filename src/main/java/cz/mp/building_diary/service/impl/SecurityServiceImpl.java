package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.exception.UserNotFoundException;
import cz.mp.building_diary.repository.UserRepository;
import cz.mp.building_diary.service.SecurityService;
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
                .flatMap(userRepository::findByEmail)
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
}