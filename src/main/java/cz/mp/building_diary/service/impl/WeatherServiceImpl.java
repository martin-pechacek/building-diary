package cz.mp.building_diary.service.impl;

import cz.mp.building_diary.dto.WeatherDto;
import cz.mp.building_diary.enums.WeatherCodes;
import cz.mp.building_diary.exception.CoordinatesNotFoundException;
import cz.mp.building_diary.service.WeatherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger LOG = LoggerFactory.getLogger(WeatherServiceImpl.class);
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search?name={postalCode}&countryCode={country}&count=1";
    private static final String HISTORICAL_URL = "https://archive-api.open-meteo.com/v1/archive?latitude={lat}&longitude={lon}&start_date={date}&end_date={date}&daily=temperature_2m_mean,weather_code";
    private static final String FORECAST_URL = "https://api.open-meteo.com/v1/forecast?latitude={lat}&longitude={lon}&daily=temperature_2m_mean,weather_code&start_date={date}&end_date={date}";

    private final RestClient restClient = RestClient.create();

    @Override
    @Cacheable(value = "weather", key = "#postalCode.toLowerCase() + '-' + #country.toLowerCase() + '-' + #date.toString()")
    public Optional<WeatherDto> getWeather(String postalCode, String country, LocalDate date) {
        try {
            double[] coordinates = getCoordinates(postalCode, country).orElseThrow(() -> new CoordinatesNotFoundException(
                    "Coordinates not found for postal code " + postalCode + ", country " + country + ". Fill weather manually"));

            double lat = coordinates[0];
            double lon = coordinates[1];
            String dateStr = date.toString();

            String url = date.isBefore(LocalDate.now()) ? HISTORICAL_URL : FORECAST_URL;

            var response = restClient.get()
                    .uri(url, lat, lon, dateStr, dateStr)
                    .retrieve()
                    .body(WeatherResponse.class);

            if (response == null || response.daily() == null) {
                return Optional.empty();
            }

            Double temperature = Optional.ofNullable(response.daily().temperature2mMean())
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .orElse(null);

            Integer weatherCode = Optional.ofNullable(response.daily().weatherCode())
                    .filter(list -> !list.isEmpty())
                    .map(List::getFirst)
                    .orElse(null);

            String condition = Optional.ofNullable(weatherCode)
                    .map(WeatherCodes::fromCode)
                    .orElse("");

            WeatherDto weather = new WeatherDto(temperature, condition);

            LOG.info("Fetched weather for {} on {}: {}, {}", postalCode, date, weather.temperature(), weather.condition());
            return Optional.of(weather);

        } catch (CoordinatesNotFoundException e) {
            throw e;
        } catch (Exception e) {
            LOG.error("Failed to fetch weather for {} on {}: {}", postalCode, date, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<double[]> getCoordinates(String postalCode, String country) {
        try {
            var response = restClient.get()
                    .uri(GEOCODING_URL, postalCode, country)
                    .retrieve()
                    .body(GeocodingResponse.class);

            return Optional.ofNullable(response)
                    .map(GeocodingResponse::results)
                    .filter(results -> !CollectionUtils.isEmpty(results))
                    .map(List::getFirst)
                    .map(l -> new double[]{l.latitude(), l.longitude()});

        } catch (Exception e) {
            LOG.error("Failed to geocode postal code {}: {}", postalCode, e.getMessage());
            return Optional.empty();
        }
    }

    private record GeocodingResponse(List<GeoLocation> results) {
    }

    private record GeoLocation(double latitude, double longitude) {
    }

    private record WeatherResponse(DailyWeather daily) {
    }

    private record DailyWeather(List<Double> temperature2mMean, List<Integer> weatherCode) {
    }
}