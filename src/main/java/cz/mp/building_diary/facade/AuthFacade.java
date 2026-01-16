package cz.mp.building_diary.facade;

import cz.mp.building_diary.dto.LoginDto;
import cz.mp.building_diary.dto.UserRegistrationDto;

public interface AuthFacade {

    LoginDto register(UserRegistrationDto dto);

    LoginDto login(LoginDto dto);

    LoginDto refresh(String refreshToken);
}