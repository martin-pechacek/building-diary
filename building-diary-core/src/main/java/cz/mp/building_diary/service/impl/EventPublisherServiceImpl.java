package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.config.RabbitMqConfig;
import cz.mp.building_diary.dto.event.EmailVerificationEvent;
import cz.mp.building_diary.dto.event.ProjectStatusChangedEvent;
import cz.mp.building_diary.service.EventPublisherService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisherServiceImpl implements EventPublisherService {

    private static final Logger LOG = LoggerFactory.getLogger(EventPublisherServiceImpl.class);

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishEmailVerification(EmailVerificationEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY_EMAIL_VERIFICATION, event);
        LOG.info("Published email verification event for user: {}", event.email());
    }

    @Override
    public void publishProjectStatusChanged(ProjectStatusChangedEvent event) {
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY_PROJECT_STATUS_CHANGED, event);
        LOG.info("Published project status changed event for project: {} status: {}", event.projectId(), event.status());
    }
}