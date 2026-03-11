package cz.mp.notification_service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    static final String EXCHANGE = "construction-site-diary.events";
    static final String DLQ = "construction-site-diary.dlq";

    static final String QUEUE_EMAIL_VERIFICATION = "notification.email-verification";
    static final String QUEUE_EMAIL_VERIFIED = "notification.email-verified";
    static final String QUEUE_PROJECT_STATUS_CHANGED = "notification.project-status-changed";

    static final String RK_EMAIL_VERIFICATION = "notification.user.email-verification";
    static final String RK_EMAIL_VERIFIED = "notification.user.email-verified";
    static final String RK_PROJECT_STATUS_CHANGED = "notification.project.status-changed";

    @Bean
    public DirectExchange constructionSiteDiaryExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Queue emailVerificationQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL_VERIFICATION)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue emailVerifiedQueue() {
        return QueueBuilder.durable(QUEUE_EMAIL_VERIFIED)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue projectStatusChangedQueue() {
        return QueueBuilder.durable(QUEUE_PROJECT_STATUS_CHANGED)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Binding emailVerificationBinding(Queue emailVerificationQueue, DirectExchange constructionSiteDiaryExchange) {
        return BindingBuilder.bind(emailVerificationQueue).to(constructionSiteDiaryExchange).with(RK_EMAIL_VERIFICATION);
    }

    @Bean
    public Binding emailVerifiedBinding(Queue emailVerifiedQueue, DirectExchange constructionSiteDiaryExchange) {
        return BindingBuilder.bind(emailVerifiedQueue).to(constructionSiteDiaryExchange).with(RK_EMAIL_VERIFIED);
    }

    @Bean
    public Binding projectStatusChangedBinding(Queue projectStatusChangedQueue, DirectExchange constructionSiteDiaryExchange) {
        return BindingBuilder.bind(projectStatusChangedQueue).to(constructionSiteDiaryExchange).with(RK_PROJECT_STATUS_CHANGED);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         JacksonJsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
