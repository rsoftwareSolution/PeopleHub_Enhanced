
package com.demo.department.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class AppConfig {

    @Value("${app.admin-username}")
    private String adminUsername;

    @Value("${app.admin-password}")
    private String adminPassword;

    @Value("${app.user-username}")
    private String userUsername;

    @Value("${app.user-password}")
    private String userPassword;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-minutes:60}")
    private long jwtExpirationMinutes;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Value("${app.employee.service-url:http://localhost:8081}")
    private String employeeServiceUrl;
}
