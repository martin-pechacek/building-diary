package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.dto.UserRegistrationResponseDto;

public interface UserService {

    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto);
}