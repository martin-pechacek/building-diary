package cz.mp.building_diary.listener;

import cz.mp.building_diary.config.RabbitMqConfig;
import cz.mp.building_diary.dto.event.EmailVerificationEvent;
import cz.mp.building_diary.dto.event.ProjectStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationEventListener.class);

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener
    public void onEmailVerification(EmailVerificationEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY_EMAIL_VERIFICATION, event);
        LOG.info("Published email verification event for user: {}", event.email());
    }

    @TransactionalEventListener
    public void onProjectStatusChanged(ProjectStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY_PROJECT_STATUS_CHANGED, event);
        LOG.info("Published project status changed event for project: {} status: {}", event.projectId(), event.status());
    }
}