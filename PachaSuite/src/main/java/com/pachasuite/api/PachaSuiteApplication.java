package com.pachasuite.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@OpenAPIDefinition(
        info = @Info(
                title = "Pacha Suite API",
                version = "1.0.0",
                description = "Sistema de reservas Hotel Boutique Pacha Suite – Juliaca, Puno",
                contact = @Contact(name = "Pacha Suite", email = "admin@pachasuite.com")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@EnableRetry
@EnableAsync
public class PachaSuiteApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Lima"));
        SpringApplication.run(PachaSuiteApplication.class, args);
    }
}