/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.domain.model.WeatherStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherGovService {
    @Async("weatherTaskExecutor")
    public CompletableFuture<WeatherData> getWeather(String city) {
        WeatherData data = new WeatherData();
        data.setSource("WeatherGov");
        data.setTemperature(22.5);
        data.setHumidity(65.0);
        data.setStatus(WeatherStatus.SUCCESS);
        return CompletableFuture.completedFuture(data);
    }
}
