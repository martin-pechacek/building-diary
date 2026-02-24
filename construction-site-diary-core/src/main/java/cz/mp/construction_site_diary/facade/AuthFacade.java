package cz.mp.construction_site_diary.facade;

import cz.mp.construction_site_diary.dto.LoginDto;
import cz.mp.construction_site_diary.dto.UserRegistrationDto;

public interface AuthFacade {

    LoginDto register(UserRegistrationDto dto);

    LoginDto login(LoginDto dto);

    LoginDto refresh(String refreshToken);

    void verifyEmail(String token);
}