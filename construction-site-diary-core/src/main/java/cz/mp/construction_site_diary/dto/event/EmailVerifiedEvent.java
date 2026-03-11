package cz.mp.construction_site_diary.dto.event;

public record EmailVerifiedEvent(String userId, String email) {}