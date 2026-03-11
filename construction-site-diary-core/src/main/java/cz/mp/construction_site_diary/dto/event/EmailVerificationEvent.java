package cz.mp.construction_site_diary.dto.event;

public record EmailVerificationEvent(
        String userId,
        String email,
        String verificationUrl
) {}