package cz.mp.notification_service.config;

import cz.mp.notification_service.properties.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class PropertiesConfig {
}