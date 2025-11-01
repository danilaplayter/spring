/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.domain.model.WeatherStatus;

class WeatherAggregationServiceTest {

    private WeatherGovService weatherGovService;
    private OpenWeatherService openWeatherService;
    private AccuWeatherService accuWeatherService;
    private WeatherAggregationService weatherAggregationService;

    @BeforeEach
    void setUp() {
        weatherGovService = Mockito.mock(WeatherGovService.class);
        openWeatherService = Mockito.mock(OpenWeatherService.class);
        accuWeatherService = Mockito.mock(AccuWeatherService.class);
        weatherAggregationService =
                new WeatherAggregationService(
                        weatherGovService, openWeatherService, accuWeatherService);
    }

    @Test
    void aggregateWeather_AllServicesSuccess_ReturnsAllData() {
        // Arrange
        String city = "Minsk";
        WeatherData govData =
                WeatherData.builder()
                        .source("WeatherGov")
                        .temperature(22.5)
                        .humidity(65.0)
                        .status(WeatherStatus.SUCCESS)
                        .build();
        WeatherData openWeatherData =
                WeatherData.builder()
                        .source("OpenWeather")
                        .temperature(23.1)
                        .humidity(62.0)
                        .status(WeatherStatus.SUCCESS)
                        .build();
        WeatherData accuWeatherData =
                WeatherData.builder()
                        .source("AccuWeather")
                        .temperature(21.8)
                        .humidity(68.0)
                        .status(WeatherStatus.SUCCESS)
                        .build();

        Mockito.when(weatherGovService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(govData));
        Mockito.when(openWeatherService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(openWeatherData));
        Mockito.when(accuWeatherService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(accuWeatherData));

        // Act
        List<WeatherData> result = weatherAggregationService.aggregateWeather(city);

        // Assert
        Assertions.assertEquals(3, result.size());
        Assertions.assertTrue(
                result.stream().allMatch(data -> data.getStatus() == WeatherStatus.SUCCESS));

        List<String> sources =
                result.stream().map(WeatherData::getSource).collect(Collectors.toList());
        Assertions.assertTrue(sources.contains("WeatherGov"));
        Assertions.assertTrue(sources.contains("OpenWeather"));
        Assertions.assertTrue(sources.contains("AccuWeather"));

        Mockito.verify(weatherGovService).getWeather(city);
        Mockito.verify(openWeatherService).getWeather(city);
        Mockito.verify(accuWeatherService).getWeather(city);
    }

    @Test
    void aggregateWeather_ServiceTimeout_ReturnsTimeoutStatus() {
        // Arrange
        String city = "Minsk";
        WeatherData successData =
                WeatherData.builder()
                        .source("WeatherGov")
                        .temperature(22.5)
                        .status(WeatherStatus.SUCCESS)
                        .build();

        CompletableFuture<WeatherData> timeoutFuture = new CompletableFuture<>();

        Mockito.when(weatherGovService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(successData));
        Mockito.when(openWeatherService.getWeather(city))
                .thenReturn(timeoutFuture); // Never completes
        Mockito.when(accuWeatherService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(successData));

        // Act
        List<WeatherData> result = weatherAggregationService.aggregateWeather(city);

        // Assert
        Assertions.assertEquals(3, result.size());

        WeatherData timeoutData = null;
        for (WeatherData data : result) {
            if ("OpenWeather".equals(data.getSource())) {
                timeoutData = data;
                break;
            }
        }

        Assertions.assertNotNull(timeoutData);
        Assertions.assertEquals(WeatherStatus.TIMEOUT, timeoutData.getStatus());
        Assertions.assertTrue(timeoutData.getErrorMessage().contains("Request timeout after"));
        Assertions.assertNull(timeoutData.getTemperature());
    }

    @Test
    void aggregateWeather_ServiceThrowsException_ReturnsErrorStatus() {
        // Arrange
        String city = "Minsk";
        WeatherData successData =
                WeatherData.builder()
                        .source("WeatherGov")
                        .temperature(22.5)
                        .status(WeatherStatus.SUCCESS)
                        .build();

        CompletableFuture<WeatherData> failingFuture = new CompletableFuture<>();
        failingFuture.completeExceptionally(new RuntimeException("Service unavailable"));

        Mockito.when(weatherGovService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(successData));
        Mockito.when(openWeatherService.getWeather(city)).thenReturn(failingFuture);
        Mockito.when(accuWeatherService.getWeather(city))
                .thenReturn(CompletableFuture.completedFuture(successData));

        // Act
        List<WeatherData> result = weatherAggregationService.aggregateWeather(city);

        // Assert
        Assertions.assertEquals(3, result.size());

        WeatherData errorData = null;
        for (WeatherData data : result) {
            if ("OpenWeather".equals(data.getSource())) {
                errorData = data;
                break;
            }
        }

        Assertions.assertNotNull(errorData);
        Assertions.assertEquals(WeatherStatus.ERROR, errorData.getStatus());
        Assertions.assertTrue(
                errorData.getErrorMessage().contains("Unexpected error: Service unavailable"));
    }

    @Test
    void aggregateWeather_AllServicesFail_ReturnsAllErrors() {
        // Arrange
        String city = "UnknownCity";

        CompletableFuture<WeatherData> govFailure = new CompletableFuture<>();
        govFailure.completeExceptionally(new RuntimeException("City not found"));

        CompletableFuture<WeatherData> openWeatherFailure = new CompletableFuture<>();
        openWeatherFailure.completeExceptionally(new RuntimeException("API limit exceeded"));

        CompletableFuture<WeatherData> accuWeatherTimeout = new CompletableFuture<>();

        Mockito.when(weatherGovService.getWeather(city)).thenReturn(govFailure);
        Mockito.when(openWeatherService.getWeather(city)).thenReturn(openWeatherFailure);
        Mockito.when(accuWeatherService.getWeather(city)).thenReturn(accuWeatherTimeout);

        // Act
        List<WeatherData> result = weatherAggregationService.aggregateWeather(city);

        // Assert
        Assertions.assertEquals(3, result.size());

        int errorCount = 0;
        int timeoutCount = 0;
        for (WeatherData data : result) {
            if (data.getStatus() == WeatherStatus.ERROR) {
                errorCount++;
            } else if (data.getStatus() == WeatherStatus.TIMEOUT) {
                timeoutCount++;
            }
        }

        Assertions.assertEquals(2, errorCount);
        Assertions.assertEquals(1, timeoutCount);

        // Все данные должны иметь null temperature
        for (WeatherData data : result) {
            Assertions.assertNull(data.getTemperature());
        }
    }
}
