package org.hung.poc.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages={"org.hung.poc.integration","org.hung.stock"})
public class MqttAdapterApplication {
    
    public static void main(String[] args) {
		SpringApplication.run(MqttAdapterApplication.class, args);
	}
}
