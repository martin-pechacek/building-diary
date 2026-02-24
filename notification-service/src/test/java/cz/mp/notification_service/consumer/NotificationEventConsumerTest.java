package cz.mp.notification_service.consumer;

import cz.mp.notification_service.dto.EmailVerificationEvent;
import cz.mp.notification_service.dto.ProjectStatusChangedEvent;
import cz.mp.notification_service.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventConsumerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private NotificationEventConsumer consumer;

    @Test
    void shouldDelegateEmailVerificationToEmailService() {
        EmailVerificationEvent event = new EmailVerificationEvent(
                "user-id", "user@test.com", "http://localhost:8080/verify?token=abc"
        );

        consumer.onEmailVerification(event);

        verify(emailService).sendEmailVerification(event);
    }

    @Test
    void shouldDelegateProjectStatusChangedToEmailService() {
        ProjectStatusChangedEvent event = new ProjectStatusChangedEvent(
                "proj-id", "Test Project", "owner@test.com", null, "IN_PROGRESS", LocalDate.now()
        );

        consumer.onProjectStatusChanged(event);

        verify(emailService).sendProjectStatusChanged(event);
    }
}
