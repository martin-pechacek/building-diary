package cz.mp.construction_site_diary.event;

public record EmailVerificationEvent(
        String userId,
        String email,
        String verificationUrl
) {}