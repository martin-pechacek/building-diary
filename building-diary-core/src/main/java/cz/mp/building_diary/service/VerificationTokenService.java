package cz.mp.building_diary.service;

import cz.mp.building_diary.entity.User;
import cz.mp.building_diary.entity.VerificationToken;

public interface VerificationTokenService {

    String createToken(User user, String tokenType);

    VerificationToken validateAndUseToken(String token);
}