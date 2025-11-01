/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.mentee.power.api.generated.controller.WeatherApi;
import ru.mentee.power.api.generated.dto.AggregatedWeatherReport;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.mapper.WeatherMapper;
import ru.mentee.power.service.WeatherAggregationService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WeatherController implements WeatherApi {

    private final WeatherAggregationService aggregationService;
    private final WeatherMapper weatherMapper;

    @Override
    public ResponseEntity<AggregatedWeatherReport> getWeather(String city) {
        log.info("Получен запрос на погоду для города: {}", city);

        List<WeatherData> weatherDataList = aggregationService.aggregateWeather(city);
        AggregatedWeatherReport report = weatherMapper.toAggregatedReport(city, weatherDataList);

        log.info("Возвращаем агрегированный отчет для города: {}", city);
        return ResponseEntity.ok(report);
    }
}
