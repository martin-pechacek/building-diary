package cz.mp.notification_service.dto;

public record EmailVerificationEvent(
        String userId,
        String email,
        String verificationUrl
) {}