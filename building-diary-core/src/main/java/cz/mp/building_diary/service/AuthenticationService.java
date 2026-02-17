package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.LoginDto;

public interface AuthenticationService {

    LoginDto login(LoginDto dto);

    LoginDto refresh(String refreshToken);
}