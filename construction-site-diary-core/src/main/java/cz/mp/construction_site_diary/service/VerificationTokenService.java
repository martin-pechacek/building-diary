package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.entity.User;
import cz.mp.construction_site_diary.entity.VerificationToken;
import cz.mp.construction_site_diary.enums.TokenType;

public interface VerificationTokenService {

    String createToken(User user, TokenType tokenType);

    VerificationToken validateAndUseToken(String token);
}