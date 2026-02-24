package cz.mp.notification_service.service;

import cz.mp.notification_service.dto.EmailVerificationEvent;
import cz.mp.notification_service.dto.ProjectStatusChangedEvent;

public interface EmailService {

    void sendEmailVerification(EmailVerificationEvent event);

    void sendProjectStatusChanged(ProjectStatusChangedEvent event);
}