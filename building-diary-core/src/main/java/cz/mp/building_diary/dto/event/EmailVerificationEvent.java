package cz.mp.building_diary.dto.event;

public record EmailVerificationEvent(
        String userId,
        String email,
        String verificationToken
) {}