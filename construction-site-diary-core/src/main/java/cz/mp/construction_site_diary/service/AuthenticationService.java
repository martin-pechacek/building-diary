package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.dto.LoginDto;

public interface AuthenticationService {

    LoginDto login(LoginDto dto);

    LoginDto refresh(String refreshToken);
}