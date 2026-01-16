package cz.mp.building_diary.facade.impl;

import cz.mp.building_diary.dto.LoginDto;
import cz.mp.building_diary.dto.UserRegistrationDto;
import cz.mp.building_diary.facade.AuthFacade;
import cz.mp.building_diary.service.AuthenticationService;
import cz.mp.building_diary.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {

    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Override
    public LoginDto register(UserRegistrationDto dto) {
        userService.registerUser(dto);
        return authenticationService.login(new LoginDto(dto.email(), dto.password(), null, null, null));
    }

    @Override
    public LoginDto login(LoginDto dto) {
        return authenticationService.login(dto);
    }

    @Override
    public LoginDto refresh(String refreshToken) {
        return authenticationService.refresh(refreshToken);
    }
}