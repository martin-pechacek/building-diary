package cz.mp.construction_site_diary.service;

public interface EmailVerificationService extends VerificationTokenService {

    void verifyEmail(String token);
}