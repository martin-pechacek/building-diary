package cz.mp.notification_service.dto;

public record EmailVerifiedEvent(String userId, String email) {}