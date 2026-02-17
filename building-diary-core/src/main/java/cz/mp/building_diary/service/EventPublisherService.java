package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.event.EmailVerificationEvent;
import cz.mp.building_diary.dto.event.ProjectStatusChangedEvent;

public interface EventPublisherService {

    void publishEmailVerification(EmailVerificationEvent event);

    void publishProjectStatusChanged(ProjectStatusChangedEvent event);
}