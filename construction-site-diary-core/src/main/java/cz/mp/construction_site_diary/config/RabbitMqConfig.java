package cz.mp.construction_site_diary.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String EXCHANGE = "construction-site-diary.events";

    public static final String ROUTING_KEY_EMAIL_VERIFICATION = "notification.user.email-verification";
    public static final String ROUTING_KEY_EMAIL_VERIFIED = "notification.user.email-verified";
    public static final String ROUTING_KEY_PROJECT_STATUS_CHANGED = "notification.project.status-changed";

    @Bean
    public DirectExchange constructionSiteDiaryExchange() {
        return new DirectExchange(EXCHANGE, true, false);
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