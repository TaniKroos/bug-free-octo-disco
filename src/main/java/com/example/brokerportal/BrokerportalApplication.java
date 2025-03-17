package com.example.brokerportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
		"com.example.brokerportal.quoteservice",
		"com.example.brokerportal.config",
		"com.example.brokerportal.authservice",
		"com.example.brokerportal.common"
})
@EnableAsync
public class BrokerportalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerportalApplication.class, args);
	}

}
