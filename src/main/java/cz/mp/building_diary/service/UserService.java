package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.LoginRequestDto;
import cz.mp.building_diary.dto.LoginResponseDto;
import cz.mp.building_diary.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.dto.UserRegistrationResponseDto;
import jakarta.servlet.http.HttpSession;

public interface UserService {

    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto);

    LoginResponseDto login(LoginRequestDto requestDto, HttpSession session);

    LoginResponseDto refreshSession(HttpSession session);
}
