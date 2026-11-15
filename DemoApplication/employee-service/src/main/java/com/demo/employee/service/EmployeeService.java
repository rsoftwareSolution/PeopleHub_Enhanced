package com.demo.employee.service;

import com.demo.employee.model.Employee;
import com.demo.employee.repository.EmployeeRepository;
import com.demo.employee.exception.ResourceNotFoundException;
import com.demo.employee.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class EmployeeService {
    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    public List<Employee> findAll() {
        return repository.findAll(Sort.by("name").ascending());
    }

    public Page<Employee> search(String query, Integer departmentId, Pageable pageable) {
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return repository.searchEmployees(normalizedQuery, departmentId, pageable);
    }

    public List<Employee> findByDepartment(Integer departmentId) {
        return repository.findByDepartmentIdOrderByNameAsc(departmentId);
    }

    public long countByDepartment(Integer departmentId) {
        return repository.countByDepartmentId(departmentId);
    }

    public List<com.demo.employee.dto.DepartmentCount> departmentCounts() {
        return repository.countEmployeesByDepartment().stream()
                .map(row -> new com.demo.employee.dto.DepartmentCount((Integer) row[0], (Long) row[1])).toList();
    }

    public Employee findById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
    }

    @Transactional
    public Employee save(Employee item) {
        item.setId(null);
        item.setEmail(normalize(item.getEmail()));
        if (repository.existsByEmailIgnoreCase(item.getEmail()))
            throw new ConflictException("An employee with this email already exists.");
        return repository.save(item);
    }

    @Transactional
    public Employee update(Integer id, Employee item) {
        Employee old = findById(id);
        String email = normalize(item.getEmail());
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id))
            throw new ConflictException("An employee with this email already exists.");
        old.setName(item.getName().trim());
        old.setEmail(email);
        old.setDepartmentId(item.getDepartmentId());
        return repository.save(old);
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Employee not found: " + id);
        repository.deleteById(id);
    }

    private String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }
}
