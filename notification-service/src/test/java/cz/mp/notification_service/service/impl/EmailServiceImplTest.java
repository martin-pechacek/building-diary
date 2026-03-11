package cz.mp.notification_service.service.impl;

import cz.mp.notification_service.dto.EmailVerificationEvent;
import cz.mp.notification_service.dto.ProjectStatusChangedEvent;
import cz.mp.notification_service.entity.NotificationLog;
import cz.mp.notification_service.properties.MailProperties;
import cz.mp.notification_service.repository.NotificationLogRepository;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    @Mock
    private MailProperties mailProperties;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Nested
    class SendEmailVerification {

        @Test
        void shouldSendEmailAndLogNotification() {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("email-verification"), any(Context.class))).thenReturn("<html>verify</html>");
            when(mailProperties.from()).thenReturn("noreply@test.local");

            emailService.sendEmailVerification(new EmailVerificationEvent(
                    "user-id", "user@test.com", "http://localhost:8080/verify?token=abc"
            ));

            verify(mailSender).send(mimeMessage);
            verify(notificationLogRepository).save(any(NotificationLog.class));
        }
    }

    @Nested
    class SendProjectStatusChanged {

        @Test
        void shouldSendEmailToOwnerOnly() {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(eq("project-status-changed"), any(Context.class))).thenReturn("<html>status</html>");
            when(mailProperties.from()).thenReturn("noreply@test.local");

            emailService.sendProjectStatusChanged(new ProjectStatusChangedEvent(
                    "proj-id", "Project Alpha", "owner@test.com", null, "IN_PROGRESS", LocalDate.now()
            ));

            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
        }

        @Test
        void shouldSendEmailToBothOwnerAndManagerWhenDifferent() {
            when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class), mock(MimeMessage.class));
            when(templateEngine.process(eq("project-status-changed"), any(Context.class))).thenReturn("<html>status</html>");
            when(mailProperties.from()).thenReturn("noreply@test.local");

            emailService.sendProjectStatusChanged(new ProjectStatusChangedEvent(
                    "proj-id", "Project Alpha", "owner@test.com", "manager@test.com", "IN_PROGRESS", LocalDate.now()
            ));

            verify(mailSender, times(2)).send(any(MimeMessage.class));
            verify(notificationLogRepository, times(2)).save(any(NotificationLog.class));
        }

        @Test
        void shouldSendEmailToOwnerOnlyWhenManagerSameAsOwner() {
            MimeMessage mimeMessage = mock(MimeMessage.class);
            when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
            when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>status</html>");
            when(mailProperties.from()).thenReturn("noreply@test.local");

            emailService.sendProjectStatusChanged(new ProjectStatusChangedEvent(
                    "proj-id", "Project Alpha", "same@test.com", "same@test.com", "IN_PROGRESS", LocalDate.now()
            ));

            verify(mailSender, times(1)).send(any(MimeMessage.class));
            verify(notificationLogRepository, times(1)).save(any(NotificationLog.class));
        }
    }
}
