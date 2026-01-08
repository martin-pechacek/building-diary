package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.WeatherDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
    void shouldReturnEmptyForInvalidCity() {
        Optional<WeatherDto> weather = weatherService.getWeather("NonExistentCity12345", "XX", LocalDate.now());

        assertThat(weather).isEmpty();
    }
}