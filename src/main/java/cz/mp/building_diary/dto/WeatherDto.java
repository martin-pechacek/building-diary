package cz.mp.building_diary.dto;

import java.io.Serializable;

public record WeatherDto(
        Double temperature,
        String condition
) implements Serializable {
}