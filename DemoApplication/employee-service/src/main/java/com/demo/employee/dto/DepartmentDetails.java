package com.demo.employee.dto;

import com.demo.employee.model.Employee;

import java.util.List;
import java.util.Map;

public record DepartmentDetails(Map<String, Object> department, List<Employee> employees, long employeeCount) {
}
