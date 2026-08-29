package ru.polymetal.labManufacture;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Компонент приложения ServletInitializer.
 *
 * @author Tatarinov Anton
 */
public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(LabManufactureApplication.class);
	}

}
