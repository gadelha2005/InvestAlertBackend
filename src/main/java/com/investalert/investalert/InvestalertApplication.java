package com.investalert.investalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InvestalertApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvestalertApplication.class, args);
	}

}
