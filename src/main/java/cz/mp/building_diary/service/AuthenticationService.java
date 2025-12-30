package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.LoginRequestDto;
import cz.mp.building_diary.dto.LoginResponseDto;
import jakarta.servlet.http.HttpSession;

public interface AuthenticationService {

    String SESSION_AUTH_INFO = "auth_info";

    LoginResponseDto login(LoginRequestDto request, HttpSession session);

    LoginResponseDto refreshSession(HttpSession session);
}