package com.demo.employee.controller;

import com.demo.employee.config.AppConfig;
import com.demo.employee.config.Constants;
import com.demo.employee.security.JwtService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.demo.employee.config.Constants.INVALID_CREDENTIALS;

@Data
@RestController
@RequestMapping(Constants.AUTH_ENDPOINT)
public class AuthController {
    private final JwtService jwt;

    private final AppConfig appConfig;

    @PostMapping(Constants.TOKEN_ENDPOINT)
    public Map<String, String> token(@RequestBody Map<String, String> body) {
        String u = body.getOrDefault(Constants.USERNAME_STRING, ""), p = body.getOrDefault(Constants.PASSWORD_STRING, "");
        if (!((appConfig.getAdminUsername().equals(u) && appConfig.getAdminPassword().equals(p))
                || (appConfig.getUserUsername().equals(u) && appConfig.getUserPassword().equals(p))))
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED,
                    INVALID_CREDENTIALS);
        return Map.of(Constants.ACCESS_TOKEN, jwt.issue(u, Constants.ADMIN_STRING.equals(u)
                ? Constants.ADMIN : Constants.USER), Constants.TOKEN_TYPE, Constants.BEARER);
    }
}
