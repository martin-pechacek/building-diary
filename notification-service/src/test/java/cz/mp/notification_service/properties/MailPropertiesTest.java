package cz.mp.notification_service.properties;

import cz.mp.notification_service.config.PropertiesConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MailPropertiesTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(PropertiesConfig.class);

    @Test
    void shouldBindFromProperty() {
        contextRunner
                .withPropertyValues("mail.from=noreply@example.com")
                .run(ctx -> {
                    MailProperties props = ctx.getBean(MailProperties.class);
                    assertThat(props.from()).isEqualTo("noreply@example.com");
                });
    }

    @Test
    void shouldBindNullWhenFromPropertyIsMissing() {
        contextRunner.run(ctx -> {
            MailProperties props = ctx.getBean(MailProperties.class);
            assertThat(props.from()).isNull();
        });
    }
}
