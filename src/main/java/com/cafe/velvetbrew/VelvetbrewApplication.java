package com.cafe.velvetbrew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class VelvetbrewApplication {

	public static void main(String[] args) {
		SpringApplication.run(VelvetbrewApplication.class, args);
	}

}
