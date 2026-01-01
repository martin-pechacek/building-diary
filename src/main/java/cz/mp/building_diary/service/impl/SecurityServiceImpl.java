package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.SessionAuthenticationInfoDto;
import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.exception.UserNotFoundException;
import cz.mp.building_diary.repository.UserRepository;
import cz.mp.building_diary.service.AuthenticationService;
import cz.mp.building_diary.service.SecurityService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SecurityServiceImpl implements SecurityService {

    private final HttpSession httpSession;
    private final UserRepository userRepository;

    @Override
    public User getCurrentUser() {
        SessionAuthenticationInfoDto authInfo = (SessionAuthenticationInfoDto)
                httpSession.getAttribute(AuthenticationService.SESSION_AUTH_INFO);

        if (authInfo == null) {
            throw new UserNotFoundException("No authenticated user");
        }

        return userRepository.findByEmail(authInfo.email())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + authInfo.email()));
    }
}