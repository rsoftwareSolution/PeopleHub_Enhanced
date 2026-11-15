package com.demo.department.repository;

import com.demo.department.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Integer id);

    Page<Department> findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(String name, String location, Pageable pageable);
}
