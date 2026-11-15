package com.demo.employee.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "employees", indexes = {
        @Index(name = "idx_employee_department", columnList = "department_id"),
        @Index(name = "idx_employee_name", columnList = "name")
})
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank
    @Email
    @Size(max = 180)
    @Column(nullable = false, unique = true, length = 180)
    private String email;

    @NotNull
    @Positive
    @Column(name = "department_id", nullable = false)
    private Integer departmentId;
}
