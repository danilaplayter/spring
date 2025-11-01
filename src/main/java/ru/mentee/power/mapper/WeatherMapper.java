/* @MENTEE_POWER (C)2025 */
package ru.mentee.power.mapper;

import static ru.mentee.power.domain.model.WeatherStatus.SUCCESS;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mentee.power.api.generated.dto.AggregatedWeatherReport;
import ru.mentee.power.api.generated.dto.WeatherReport;
import ru.mentee.power.domain.model.WeatherData;
import ru.mentee.power.domain.model.WeatherStatus;

@Mapper(componentModel = "spring")
public interface WeatherMapper {

    @Mapping(target = "city", source = "city")
    @Mapping(target = "reports", source = "weatherDataList")
    AggregatedWeatherReport toAggregatedReport(String city, List<WeatherData> weatherDataList);

    @Mapping(target = "status", expression = "java(mapStatus(weatherData.getStatus()))")
    WeatherReport toWeatherReport(WeatherData weatherData);

    List<WeatherReport> toWeatherReports(List<WeatherData> weatherDataList);

    default WeatherReport.StatusEnum mapStatus(WeatherStatus status) {
        if (status == null) {
            return WeatherReport.StatusEnum.ERROR;
        }
        return switch (status) {
            case SUCCESS -> WeatherReport.StatusEnum.SUCCESS;
            case TIMEOUT -> WeatherReport.StatusEnum.TIMEOUT;
            case ERROR -> WeatherReport.StatusEnum.ERROR;
        };
    }
}
