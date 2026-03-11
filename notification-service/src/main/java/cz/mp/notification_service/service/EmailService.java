package cz.mp.notification_service.service;

import cz.mp.notification_service.dto.EmailVerificationEvent;
import cz.mp.notification_service.dto.EmailVerifiedEvent;
import cz.mp.notification_service.dto.ProjectStatusChangedEvent;

public interface EmailService {

    void sendEmailVerification(EmailVerificationEvent event);

    void sendEmailVerified(EmailVerifiedEvent event);

    void sendProjectStatusChanged(ProjectStatusChangedEvent event);
}