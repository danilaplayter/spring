/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.mentee.power.api.generated.dto.AggregatedWeatherReport;
import ru.mentee.power.api.generated.dto.WeatherReport;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.domain.model.WeatherStatus;
import ru.mentee.power.mapper.WeatherMapper;
import ru.mentee.power.service.WeatherAggregationService;

class WeatherControllerTest {

    private WeatherAggregationService aggregationService;
    private WeatherMapper weatherMapper;
    private WeatherController weatherController;

    @BeforeEach
    void setUp() {
        aggregationService = Mockito.mock(WeatherAggregationService.class);
        weatherMapper = Mockito.mock(WeatherMapper.class);
        weatherController = new WeatherController(aggregationService, weatherMapper);
    }

    @Test
    void getWeather_AllServicesSuccess_ReturnsOkWithAggregatedReport() {
        // Arrange
        String city = "Minsk";
        List<WeatherData> weatherDataList = createSuccessWeatherDataList();
        AggregatedWeatherReport expectedReport =
                createAggregatedWeatherReport(city, weatherDataList);

        Mockito.when(aggregationService.aggregateWeather(city)).thenReturn(weatherDataList);
        Mockito.when(weatherMapper.toAggregatedReport(city, weatherDataList))
                .thenReturn(expectedReport);

        // Act
        ResponseEntity<AggregatedWeatherReport> response = weatherController.getWeather(city);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(city, response.getBody().getCity());
        Assertions.assertEquals(3, response.getBody().getReports().size());

        Mockito.verify(aggregationService).aggregateWeather(city);
        Mockito.verify(weatherMapper).toAggregatedReport(city, weatherDataList);
    }

    @Test
    void getWeather_SomeServicesFail_ReturnsOkWithPartialData() {
        // Arrange
        String city = "London";
        List<WeatherData> weatherDataList = createMixedWeatherDataList();
        AggregatedWeatherReport expectedReport =
                createAggregatedWeatherReport(city, weatherDataList);

        Mockito.when(aggregationService.aggregateWeather(city)).thenReturn(weatherDataList);
        Mockito.when(weatherMapper.toAggregatedReport(city, weatherDataList))
                .thenReturn(expectedReport);

        // Act
        ResponseEntity<AggregatedWeatherReport> response = weatherController.getWeather(city);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(city, response.getBody().getCity());

        // Проверяем, что есть как успешные, так и неуспешные отчеты
        List<WeatherReport> reports = response.getBody().getReports();
        Assertions.assertEquals(3, reports.size());

        boolean hasSuccess =
                reports.stream().anyMatch(r -> r.getStatus() == WeatherReport.StatusEnum.SUCCESS);
        boolean hasError =
                reports.stream().anyMatch(r -> r.getStatus() == WeatherReport.StatusEnum.ERROR);
        boolean hasTimeout =
                reports.stream().anyMatch(r -> r.getStatus() == WeatherReport.StatusEnum.TIMEOUT);

        Assertions.assertTrue(hasSuccess);
        Assertions.assertTrue(hasError || hasTimeout);

        Mockito.verify(aggregationService).aggregateWeather(city);
        Mockito.verify(weatherMapper).toAggregatedReport(city, weatherDataList);
    }

    @Test
    void getWeather_AllServicesFail_ReturnsOkWithErrorReports() {
        // Arrange
        String city = "UnknownCity";
        List<WeatherData> weatherDataList = createErrorWeatherDataList();
        AggregatedWeatherReport expectedReport =
                createAggregatedWeatherReport(city, weatherDataList);

        Mockito.when(aggregationService.aggregateWeather(city)).thenReturn(weatherDataList);
        Mockito.when(weatherMapper.toAggregatedReport(city, weatherDataList))
                .thenReturn(expectedReport);

        // Act
        ResponseEntity<AggregatedWeatherReport> response = weatherController.getWeather(city);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(city, response.getBody().getCity());

        List<WeatherReport> reports = response.getBody().getReports();
        Assertions.assertEquals(3, reports.size());

        // Все отчеты должны иметь статус ERROR или TIMEOUT
        boolean allFailed =
                reports.stream()
                        .allMatch(
                                r ->
                                        r.getStatus() == WeatherReport.StatusEnum.ERROR
                                                || r.getStatus()
                                                        == WeatherReport.StatusEnum.TIMEOUT);
        Assertions.assertTrue(allFailed);

        Mockito.verify(aggregationService).aggregateWeather(city);
        Mockito.verify(weatherMapper).toAggregatedReport(city, weatherDataList);
    }

    @Test
    void getWeather_EmptyCity_ReturnsOk() {
        // Arrange
        String city = "";
        List<WeatherData> weatherDataList = new ArrayList<>();
        AggregatedWeatherReport expectedReport = new AggregatedWeatherReport();
        expectedReport.setCity(city);
        expectedReport.setReports(new ArrayList<>());

        Mockito.when(aggregationService.aggregateWeather(city)).thenReturn(weatherDataList);
        Mockito.when(weatherMapper.toAggregatedReport(city, weatherDataList))
                .thenReturn(expectedReport);

        // Act
        ResponseEntity<AggregatedWeatherReport> response = weatherController.getWeather(city);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertNotNull(response.getBody());

        Mockito.verify(aggregationService).aggregateWeather(city);
        Mockito.verify(weatherMapper).toAggregatedReport(city, weatherDataList);
    }

    private List<WeatherData> createSuccessWeatherDataList() {
        List<WeatherData> dataList = new ArrayList<>();
        dataList.add(
                WeatherData.builder()
                        .source("WeatherGov")
                        .temperature(22.5)
                        .humidity(65.0)
                        .windSpeed(5.2)
                        .status(WeatherStatus.SUCCESS)
                        .build());
        dataList.add(
                WeatherData.builder()
                        .source("OpenWeather")
                        .temperature(23.1)
                        .humidity(62.0)
                        .windSpeed(4.8)
                        .status(WeatherStatus.SUCCESS)
                        .build());
        dataList.add(
                WeatherData.builder()
                        .source("AccuWeather")
                        .temperature(21.8)
                        .humidity(68.0)
                        .windSpeed(5.5)
                        .status(WeatherStatus.SUCCESS)
                        .build());
        return dataList;
    }

    private List<WeatherData> createMixedWeatherDataList() {
        List<WeatherData> dataList = new ArrayList<>();
        dataList.add(
                WeatherData.builder()
                        .source("WeatherGov")
                        .temperature(22.5)
                        .humidity(65.0)
                        .status(WeatherStatus.SUCCESS)
                        .build());
        dataList.add(
                WeatherData.builder()
                        .source("OpenWeather")
                        .status(WeatherStatus.ERROR)
                        .errorMessage("Service unavailable")
                        .build());
        dataList.add(
                WeatherData.builder()
                        .source("AccuWeather")
                        .status(WeatherStatus.TIMEOUT)
                        .errorMessage("Request timeout after 5 seconds")
                        .build());
        return dataList;
    }

    private List<WeatherData> createErrorWeatherDataList() {
        List<WeatherData> dataList = new ArrayList<>();
        dataList.add(
                WeatherData.builder()
                        .source("WeatherGov")
                        .status(WeatherStatus.ERROR)
                        .errorMessage("City not found")
                        .build());
        dataList.add(
                WeatherData.builder()
                        .source("OpenWeather")
                        .status(WeatherStatus.ERROR)
                        .errorMessage("API limit exceeded")
                        .build());
        dataList.add(
                WeatherData.builder()
                        .source("AccuWeather")
                        .status(WeatherStatus.TIMEOUT)
                        .errorMessage("Request timeout after 5 seconds")
                        .build());
        return dataList;
    }

    private AggregatedWeatherReport createAggregatedWeatherReport(
            String city, List<WeatherData> weatherDataList) {
        AggregatedWeatherReport report = new AggregatedWeatherReport();
        report.setCity(city);

        List<WeatherReport> weatherReports = new ArrayList<>();
        for (WeatherData data : weatherDataList) {
            WeatherReport weatherReport = new WeatherReport();
            weatherReport.setSource(data.getSource());
            weatherReport.setTemperature(data.getTemperature());
            weatherReport.setHumidity(data.getHumidity());
            weatherReport.setWindSpeed(data.getWindSpeed());
            weatherReport.setErrorMessage(data.getErrorMessage());

            if (data.getStatus() == WeatherStatus.SUCCESS) {
                weatherReport.setStatus(WeatherReport.StatusEnum.SUCCESS);
            } else if (data.getStatus() == WeatherStatus.TIMEOUT) {
                weatherReport.setStatus(WeatherReport.StatusEnum.TIMEOUT);
            } else {
                weatherReport.setStatus(WeatherReport.StatusEnum.ERROR);
            }

            weatherReports.add(weatherReport);
        }

        report.setReports(weatherReports);
        return report;
    }
}
