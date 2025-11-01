/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.domain.model.WeatherStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAggregationService {

    private final WeatherGovService weatherGovService;
    private final OpenWeatherService openWeatherService;
    private final AccuWeatherService accuWeatherService;

    private static final int TIMEOUT_SECONDS = 5;

    public List<WeatherData> aggregateWeather(String city) {
        log.info("Начинаем агрегацию погоды для города: {}", city);

        CompletableFuture<WeatherData> weatherGovFuture =
                weatherGovService
                        .getWeather(city)
                        .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .exceptionally(throwable -> handleException(throwable, "WeatherGov", city));

        CompletableFuture<WeatherData> openWeatherFuture =
                openWeatherService
                        .getWeather(city)
                        .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .exceptionally(
                                throwable -> handleException(throwable, "OpenWeather", city));

        CompletableFuture<WeatherData> accuWeatherFuture =
                accuWeatherService
                        .getWeather(city)
                        .orTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                        .exceptionally(
                                throwable -> handleException(throwable, "AccuWeather", city));

        List<WeatherData> results = new ArrayList<>();
        results.add(weatherGovFuture.join());
        results.add(openWeatherFuture.join());
        results.add(accuWeatherFuture.join());

        log.info("Агрегация завершена для города: {}. Получено {} отчетов", city, results.size());

        return results;
    }

    private WeatherData handleException(Throwable throwable, String source, String city) {
        if (throwable instanceof TimeoutException) {
            log.warn("Таймаут для источника {} при запросе погоды для {}", source, city);
            return WeatherData.builder()
                    .source(source)
                    .status(WeatherStatus.TIMEOUT)
                    .errorMessage("Request timeout after " + TIMEOUT_SECONDS + " seconds")
                    .build();
        } else {
            log.error("Ошибка при получении данных от {} для {}", source, city, throwable);
            return WeatherData.builder()
                    .source(source)
                    .status(WeatherStatus.ERROR)
                    .errorMessage("Unexpected error: " + throwable.getMessage())
                    .build();
        }
    }
}
