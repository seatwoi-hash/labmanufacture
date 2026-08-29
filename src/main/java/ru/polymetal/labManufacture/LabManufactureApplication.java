package ru.polymetal.labManufacture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Компонент приложения LabManufactureApplication.
 *
 * @author Tatarinov Anton
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class LabManufactureApplication {

	public static void main(String[] args) {
		SpringApplication.run(LabManufactureApplication.class, args);
	}

}
