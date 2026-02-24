package cz.mp.notification_service.consumer;

import cz.mp.notification_service.dto.EmailVerificationEvent;
import cz.mp.notification_service.dto.ProjectStatusChangedEvent;
import cz.mp.notification_service.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final EmailService emailService;

    @RabbitListener(queues = "#{@emailVerificationQueue.name}")
    public void onEmailVerification(EmailVerificationEvent event) {
        LOG.info("Received email verification event for: {}", event.email());
        emailService.sendEmailVerification(event);
    }

    @RabbitListener(queues = "#{@projectStatusChangedQueue.name}")
    public void onProjectStatusChanged(ProjectStatusChangedEvent event) {
        LOG.info("Received project status changed event for project: {}", event.projectId());
        emailService.sendProjectStatusChanged(event);
    }
}