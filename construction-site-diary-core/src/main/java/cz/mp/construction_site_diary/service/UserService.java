package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.dto.UserRegistrationDto;

import java.util.UUID;

public interface UserService {

    UserRegistrationDto registerUser(UserRegistrationDto dto);

    void userExists(UUID id);
}