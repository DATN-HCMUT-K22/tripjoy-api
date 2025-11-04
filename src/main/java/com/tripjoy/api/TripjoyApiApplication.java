package com.tripjoy.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication		// handle annotation ConfigurationProperties in class OpenApiProperties
@ConfigurationPropertiesScan
public class TripjoyApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripjoyApiApplication.class, args);
	}

}
