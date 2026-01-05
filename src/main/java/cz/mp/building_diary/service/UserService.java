package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.UserRegistrationRequestDto;
import cz.mp.building_diary.dto.UserRegistrationResponseDto;

import java.util.UUID;

public interface UserService {

    UserRegistrationResponseDto registerUser(UserRegistrationRequestDto requestDto);

    void userExists(UUID id);
}