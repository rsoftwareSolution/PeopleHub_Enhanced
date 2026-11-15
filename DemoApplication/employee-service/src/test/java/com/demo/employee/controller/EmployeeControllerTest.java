package com.demo.employee.controller;

import com.demo.employee.service.EmployeeService;
import com.demo.employee.client.DepartmentClient;
import com.demo.employee.model.Employee;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    EmployeeService service;
    @MockBean
    DepartmentClient departmentClient;

    @Test
    void listEndpointResponds() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isOk());
    }

    @Test
    void createValidatesDepartmentAndReturnsCreated() throws Exception {
        mvc.perform(post("/api/employees").header("Authorization", "Bearer demo")
                        .contentType("application/json")
                        .content("{\"name\":\"Asha Patil\",\"email\":\"asha@example.com\",\"departmentId\":3}"))
                .andExpect(status().isCreated());
        verify(departmentClient).getDepartment(3L, "Bearer demo");
        verify(service).save(any(Employee.class));
    }

    @Test
    void invalidEmailIsRejected() throws Exception {
        mvc.perform(post("/api/employees").header("Authorization", "Bearer demo")
                        .contentType("application/json")
                        .content("{\"name\":\"Asha Patil\",\"email\":\"not-an-email\",\"departmentId\":3}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(departmentClient);
        verify(service, never()).save(any());
    }

    @Test
    void invalidPaginationIsRejected() throws Exception {
        mvc.perform(get("/api/employees/search?page=-1&size=10")).andExpect(status().isBadRequest());
    }
}
