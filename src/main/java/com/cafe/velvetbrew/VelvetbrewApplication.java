package com.cafe.velvetbrew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@EnableScheduling
@SpringBootApplication
public class VelvetbrewApplication {

	public static void main(String[] args) {
		SpringApplication.run(VelvetbrewApplication.class, args);
	}

}
