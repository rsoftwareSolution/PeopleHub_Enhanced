package com.demo.employee.controller;

import com.demo.employee.client.DepartmentClient;
import com.demo.employee.config.Constants;
import com.demo.employee.dto.DepartmentCount;
import com.demo.employee.dto.DepartmentDetails;
import com.demo.employee.model.Employee;
import com.demo.employee.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.EMPLOYEES)
public class EmployeeDepartmentController {

    private final DepartmentClient client;
    private final EmployeeService employees;

    @GetMapping(Constants.DEPARTMENT_COUNTS)
    public List<DepartmentCount> departmentCounts() {
        return employees.departmentCounts();
    }

    @GetMapping(Constants.DEPARTMENT + Constants.DEPARTMENT_ID)
    public Map<String, Object> department(@PathVariable("id") Long id, @RequestHeader(Constants.AUTHORIZATION) String token) {
        return client.getDepartment(id, token);
    }

    @GetMapping(Constants.DEPARTMENT + Constants.DEPARTMENT_EMPLOYEES)
    public List<Employee> employeesByDepartment(@PathVariable("id") Integer id) {
        return employees.findByDepartment(id);
    }

    @GetMapping(Constants.DEPARTMENT + Constants.DEPARTMENT_COUNT)
    public Map<String, Object> employeeCount(@PathVariable("id") Integer id) {
        return Map.of(Constants.PARAM_DEPARTMENT_ID, id, Constants.EMPLOYEE_COUNT, employees.countByDepartment(id));
    }

    @GetMapping(Constants.DEPARTMENT + Constants.DEPARTMENT_DETAILS)
    public DepartmentDetails departmentDetails(@PathVariable("id") Long id, @RequestHeader(Constants.AUTHORIZATION) String token) {
        List<Employee> members = employees.findByDepartment(id.intValue());
        return new DepartmentDetails(client.getDepartment(id, token), members, members.size());
    }
}
