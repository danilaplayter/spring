/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData {
    private String source;
    private Double temperature;
    private Double humidity;
    private Double windSpeed;
    private WeatherStatus status;
    private String errorMessage;
}
