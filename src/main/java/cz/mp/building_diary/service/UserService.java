package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.UserRegistrationDto;

import java.util.UUID;

public interface UserService {

    UserRegistrationDto registerUser(UserRegistrationDto dto);

    void userExists(UUID id);
}