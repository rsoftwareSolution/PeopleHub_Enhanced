package com.demo.department.controller;

import com.demo.department.service.DepartmentService;
import com.demo.department.client.EmployeeClient;
import com.demo.department.model.Department;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {
    @Autowired
    MockMvc mvc;
    @MockBean
    DepartmentService service;
    @MockBean
    EmployeeClient employeeClient;

    @Test
    void listEndpointResponds() throws Exception {
        mvc.perform(get("/api/departments")).andExpect(status().isOk());
    }

    @Test
    void invalidDepartmentIsRejected() throws Exception {
        mvc.perform(post("/api/departments").contentType("application/json")
                        .content("{\"name\":\" \",\"location\":\"Pune\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidPaginationIsRejected() throws Exception {
        mvc.perform(get("/api/departments/search?page=0&size=500")).andExpect(status().isBadRequest());
    }

    @Test
    void departmentWithEmployeesCannotBeDeleted() throws Exception {
        when(service.findById(7)).thenReturn(new Department());
        when(employeeClient.employeeCount(7, "Bearer demo")).thenReturn(2L);
        mvc.perform(delete("/api/departments/7").header("Authorization", "Bearer demo"))
                .andExpect(status().isConflict());
        verify(service, never()).delete(7);
    }
}
