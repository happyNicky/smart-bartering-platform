package com.finalyear.liwatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LiwatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(LiwatchApplication.class, args);
	}

}
