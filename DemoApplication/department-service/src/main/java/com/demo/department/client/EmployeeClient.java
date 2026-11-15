package com.demo.department.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Component
public class EmployeeClient {
    private final RestClient client;

    public EmployeeClient(RestClient.Builder builder, @Value("${employee.service-url}") String url) {
        this.client = builder.baseUrl(url).build();
    }

    public long employeeCount(Integer departmentId, String authorization) {
        try {
            Map<String, Object> response = client.get()
                    .uri("/api/employees/department/{id}/count", departmentId)
                    .header("Authorization", authorization).retrieve().body(Map.class);
            if (response == null || !(response.get("employeeCount") instanceof Number count)) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Employee service returned an invalid count response.");
            }
            return count.longValue();
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Downstream service rejected the request.", ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Employee service is unavailable.", ex);
        }
    }

    public List<Map<String, Object>> departmentCounts(String authorization) {
        try {
            List<Map<String, Object>> response = client.get().uri("/api/employees/department-counts")
                    .header("Authorization", authorization).retrieve().body(List.class);
            return response == null ? List.of() : response;
        } catch (HttpClientErrorException ex) {
            throw new ResponseStatusException(ex.getStatusCode(), "Downstream service rejected the request.", ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Employee service is unavailable or rejected the request.", ex);
        }
    }
}
