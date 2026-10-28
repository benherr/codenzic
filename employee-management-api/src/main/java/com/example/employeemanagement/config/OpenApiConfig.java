package com.example.employeemanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI employeeManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Employee Management REST API")
                .version("1.0.0")
                .description("CRUD API for employees built with Spring Boot, JPA and Bean Validation.")
                .contact(new Contact().name("Employee Management Team")));
    }
}
