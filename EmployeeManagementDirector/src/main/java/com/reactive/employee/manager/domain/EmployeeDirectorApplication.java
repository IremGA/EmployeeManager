package com.reactive.employee.manager.domain;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@EnableRetry
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "IAKTAS - Employee Director ", version = "1.0", description = "IAKTAS - Documentation APIs v1.0"))
public class EmployeeDirectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmployeeDirectorApplication.class, args);
	}
	
}
