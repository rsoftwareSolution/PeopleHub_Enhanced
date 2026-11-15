package com.demo.department.dto;

import com.demo.department.model.Department;

public record DepartmentSummary(Department department, long employeeCount) {
}
