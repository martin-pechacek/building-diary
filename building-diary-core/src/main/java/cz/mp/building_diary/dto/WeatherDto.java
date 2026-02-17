package cz.mp.building_diary.dto;

import java.io.Serializable;

public record WeatherDto(
        Float temperature,
        String condition
) implements Serializable {
}