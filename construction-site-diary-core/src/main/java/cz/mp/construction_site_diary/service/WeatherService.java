package cz.mp.construction_site_diary.service;

import cz.mp.construction_site_diary.dto.WeatherDto;

import java.time.LocalDate;
import java.util.Optional;

public interface WeatherService {

    Optional<WeatherDto> getWeather(String postalCode, String country, LocalDate date);
}