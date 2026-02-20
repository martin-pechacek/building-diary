package cz.mp.building_diary.service;

import cz.mp.building_diary.entity.User;

public interface EmailVerificationService {

    String createVerificationToken(User user);

    void verifyEmail(String token);
}