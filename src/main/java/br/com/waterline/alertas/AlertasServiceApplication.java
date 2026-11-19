package br.com.waterline.alertas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AlertasServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlertasServiceApplication.class, args);
	}

}
