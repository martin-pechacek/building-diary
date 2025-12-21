package cz.mp.building_diary.service;

import cz.mp.building_diary.controller.v1.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.controller.v1.dto.UserRegistrationResponseDto;

public interface UserService {

    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto);
}
