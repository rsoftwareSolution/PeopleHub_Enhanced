package com.demo.employee.client;

import com.demo.employee.config.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Slf4j
@Component
public class DepartmentClient {
    private final RestClient client;

    public DepartmentClient(RestClient.Builder builder, @Value("${department.service-url}") String url) {
        this.client = builder.baseUrl(url).build();
    }

    public Map<String, Object> getDepartment(Long id, String authorization) {

        try {
            return client.get().uri("/api/departments/{id}", id).header(Constants.AUTHORIZATION, authorization).retrieve().body(new ParameterizedTypeReference<Map<String, Object>>() {
            });
        } catch (HttpStatusCodeException ex) {
            log.error("Department Service returned HTTP {}. Response: {}", ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
            throw new ResponseStatusException(ex.getStatusCode(), "Department Service error: " + ex.getResponseBodyAsString(), ex);
        } catch (ResourceAccessException ex) {
            log.error("Cannot connect to Department Service.", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cannot connect to Department Service. Check service URL and port.", ex);
        } catch (RestClientException ex) {
            log.error("Unexpected Department Service communication error.", ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Department Service communication failed.", ex);
        }
    }
}
