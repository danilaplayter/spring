/* @MENTEE_POWER (C)2025 */
package ru.mentee.power;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ProductDetailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductDetailsApplication.class, args);
    }
}
