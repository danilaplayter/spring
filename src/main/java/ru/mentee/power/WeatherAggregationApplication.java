/* @MENTEE_POWER (C)2025 */
package ru.mentee.power;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class WeatherAggregationApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherAggregationApplication.class, args);
    }
}
