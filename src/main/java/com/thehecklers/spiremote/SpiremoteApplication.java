package com.thehecklers.spiremote;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpiremoteApplication {
	@Bean
	RestOperations restOperations() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpiremoteApplication.class, args);
	}

}
