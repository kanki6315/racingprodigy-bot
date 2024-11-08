package com.racingprodigy.standings_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StandingsBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(StandingsBotApplication.class, args);
	}

}
