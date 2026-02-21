package cz.mp.construction_site_diary.dto;

import java.io.Serializable;

public record WeatherDto(
        Float temperature,
        String condition
) implements Serializable {
}