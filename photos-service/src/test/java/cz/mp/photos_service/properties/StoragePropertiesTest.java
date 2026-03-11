package cz.mp.photos_service.properties;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class StoragePropertiesTest {

    @Configuration
    @EnableConfigurationProperties(StorageProperties.class)
    static class TestConfig {}

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void shouldBindLocationProperty() {
        contextRunner
                .withPropertyValues("storage.location=/var/app/uploads")
                .run(ctx -> {
                    StorageProperties props = ctx.getBean(StorageProperties.class);
                    assertThat(props.location()).isEqualTo("/var/app/uploads");
                });
    }

    @Test
    void shouldBindNullWhenLocationPropertyIsMissing() {
        contextRunner.run(ctx -> {
            StorageProperties props = ctx.getBean(StorageProperties.class);
            assertThat(props.location()).isNull();
        });
    }
}
