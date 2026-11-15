package com.demo.employee.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Component
public class DepartmentClient {
    private final RestClient client;

    public DepartmentClient(RestClient.Builder builder, @Value("${department.service-url}") String url) {
        this.client = builder.baseUrl(url).build();
    }

    public Map<String, Object> getDepartment(Long id, String authorization) {
        try {
            Map<String, Object> body = client.get().uri("/api/departments/{id}", id)
                    .header("Authorization", authorization).retrieve().body(Map.class);
            return body;
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Downstream service rejected the request.", ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Department service is unavailable or rejected the request.", ex);
        }
    }
}
