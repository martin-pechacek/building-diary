package cz.mp.construction_site_diary.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NotFutureDateValidatorTest {

    private NotFutureDateValidator validator;

    @BeforeEach
    void setUp() {
        validator = new NotFutureDateValidator();
    }

    @Test
    void shouldReturnFalseForNullDate() {
        boolean result = validator.isValid(null, null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueForTodayDate() {
        boolean result = validator.isValid(LocalDate.now(), null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnTrueForPastDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        boolean result = validator.isValid(yesterday, null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForFutureDate() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        boolean result = validator.isValid(tomorrow, null);

        assertThat(result).isFalse();
    }

    @Test
    void shouldReturnTrueForDistantPastDate() {
        LocalDate distantPast = LocalDate.of(2020, 1, 1);

        boolean result = validator.isValid(distantPast, null);

        assertThat(result).isTrue();
    }

    @Test
    void shouldReturnFalseForDistantFutureDate() {
        LocalDate distantFuture = LocalDate.of(2030, 12, 31);

        boolean result = validator.isValid(distantFuture, null);

        assertThat(result).isFalse();
    }
}