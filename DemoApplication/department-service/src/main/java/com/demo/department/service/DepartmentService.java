package com.demo.department.service;

import com.demo.department.config.Constants;
import com.demo.department.model.Department;
import com.demo.department.repository.DepartmentRepository;
import com.demo.department.exception.ResourceNotFoundException;
import com.demo.department.exception.ConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DepartmentService {
    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    public List<Department> findAll() {
        return repository.findAll(Sort.by(Constants.SORT_NAME).ascending());
    }

    public Page<Department> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) return repository.findAll(pageable);
        return repository.findByNameContainingIgnoreCaseOrLocationContainingIgnoreCase(query.trim(), query.trim(), pageable);
    }

    public Department findById(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    @Transactional
    public Department save(Department item) {
        item.setId(null);
        item.setName(item.getName().trim());
        item.setLocation(item.getLocation().trim());
        if (repository.existsByNameIgnoreCase(item.getName()))
            throw new ConflictException("Department name already exists.");
        return repository.save(item);
    }

    @Transactional
    public Department update(Integer id, Department item) {
        Department old = findById(id);
        String name = item.getName().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id))
            throw new ConflictException("Department name already exists.");
        old.setName(name);
        old.setLocation(item.getLocation().trim());
        return repository.save(old);
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Department not found: " + id);
        repository.deleteById(id);
    }
}
