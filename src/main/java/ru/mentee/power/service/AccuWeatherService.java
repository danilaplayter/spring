/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.domain.model.WeatherStatus;

@Service
@RequiredArgsConstructor
public class AccuWeatherService {
    @Async("weatherTaskExecutor")
    public CompletableFuture<WeatherData> getWeather(String city) {
        WeatherData data = new WeatherData();
        data.setSource("AccuWeather");
        data.setTemperature(21.8);
        data.setHumidity(68.0);
        data.setStatus(WeatherStatus.SUCCESS);
        return CompletableFuture.completedFuture(data);
    }
}
