package com.example.brokerportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
		"com.example.brokerportal.quoteservice",
		"com.example.brokerportal.config",
		"com.example.brokerportal.authservice",
		"com.example.brokerportal.common"
})
public class BrokerportalApplication {

	public static void main(String[] args) {
		SpringApplication.run(BrokerportalApplication.class, args);
	}

}
