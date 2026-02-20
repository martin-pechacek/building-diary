package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.entity.User;

public interface EmailVerificationService {

    String createVerificationToken(User user);

    void verifyEmail(String token);
}