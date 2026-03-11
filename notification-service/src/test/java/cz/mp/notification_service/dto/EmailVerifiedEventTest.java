package cz.mp.notification_service.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailVerifiedEventTest {

    @Test
    void shouldCreateWithAllFields() {
        EmailVerifiedEvent event = new EmailVerifiedEvent("user-123", "user@test.com");

        assertThat(event.userId()).isEqualTo("user-123");
        assertThat(event.email()).isEqualTo("user@test.com");
    }

    @Test
    void shouldBeEqualWhenFieldsMatch() {
        EmailVerifiedEvent event1 = new EmailVerifiedEvent("user-123", "user@test.com");
        EmailVerifiedEvent event2 = new EmailVerifiedEvent("user-123", "user@test.com");

        assertThat(event1).isEqualTo(event2);
        assertThat(event1.hashCode()).isEqualTo(event2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenFieldsDiffer() {
        EmailVerifiedEvent event1 = new EmailVerifiedEvent("user-123", "user@test.com");
        EmailVerifiedEvent event2 = new EmailVerifiedEvent("user-456", "other@test.com");

        assertThat(event1).isNotEqualTo(event2);
    }

    @Test
    void shouldIncludeFieldsInToString() {
        EmailVerifiedEvent event = new EmailVerifiedEvent("user-123", "user@test.com");

        assertThat(event.toString()).contains("user-123", "user@test.com");
    }
}
