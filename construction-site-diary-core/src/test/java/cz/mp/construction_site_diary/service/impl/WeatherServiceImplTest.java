package cz.mp.construction_site_diary.service.impl;

import cz.mp.construction_site_diary.dto.WeatherDto;
import cz.mp.construction_site_diary.exception.CoordinatesNotFoundException;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
class WeatherServiceImplTest {

    private final WeatherServiceImpl weatherService = new WeatherServiceImpl();

    @Test
    void shouldReturnWeatherForValidCity() {
        Optional<WeatherDto> weather = weatherService.getWeather("Prague", "CZ", LocalDate.now());

        assertThat(weather).isPresent();
        assertThat(weather.get().temperature()).isNotNull();
    }

    @Test
    void shouldReturnWeatherForHistoricalDate() {
        LocalDate pastDate = LocalDate.now().minusDays(7);

        Optional<WeatherDto> weather = weatherService.getWeather("Prague", "CZ", pastDate);

        assertThat(weather).isPresent();
        assertThat(weather.get().temperature()).isNotNull();
    }

    @Test
    void shouldThrowExceptionForInvalidCity() {
        assertThatThrownBy(() -> weatherService.getWeather("NonExistentCity12345", "XX", LocalDate.now()))
                .isInstanceOf(CoordinatesNotFoundException.class);
    }
}