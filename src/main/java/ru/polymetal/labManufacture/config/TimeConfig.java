package ru.polymetal.labManufacture.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Конфигурация источника времени приложения.
 *
 * @author Tatarinov Anton
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock applicationClock() {
        return Clock.systemDefaultZone();
    }
}
