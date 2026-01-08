package cz.mp.building_diary.service;

import cz.mp.building_diary.dto.WeatherDto;

import java.time.LocalDate;
import java.util.Optional;

public interface WeatherService {

    Optional<WeatherDto> getWeather(String postalCode, String country, LocalDate date);
}