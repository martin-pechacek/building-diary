package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.entity.VerificationToken;

public interface VerificationTokenService {

    String createToken(User user, String tokenType);

    VerificationToken validateAndUseToken(String token);
}