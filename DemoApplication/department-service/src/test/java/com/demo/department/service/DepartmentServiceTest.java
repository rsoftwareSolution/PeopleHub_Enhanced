package com.demo.department.service;

import com.demo.department.model.Department;
import com.demo.department.repository.DepartmentRepository;
import com.demo.department.exception.ResourceNotFoundException;
import com.demo.department.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {
    @Mock
    DepartmentRepository repository;
    @InjectMocks
    DepartmentService service;

    @Test
    void findByIdReturnsEntity() {
        Department item = department(7, "Engineering", "Pune");
        when(repository.findById(7)).thenReturn(Optional.of(item));
        assertEquals(7, service.findById(7).getId());
    }

    @Test
    void missingEntityThrows() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99));
    }

    @Test
    void duplicateNameRejected() {
        Department item = department(null, "Engineering", "Pune");
        when(repository.existsByNameIgnoreCase("Engineering")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.save(item));
        verify(repository, never()).save(any());
    }

    @Test
    void updateChangesFieldsAndKeepsIdentifier() {
        Department old = department(2, "Engineering", "Pune");
        when(repository.findById(2)).thenReturn(Optional.of(old));
        when(repository.existsByNameIgnoreCaseAndIdNot("Platform", 2)).thenReturn(false);
        when(repository.save(old)).thenReturn(old);
        Department changed = service.update(2, department(null, "Platform", "Mumbai"));
        assertEquals(2, changed.getId());
        assertEquals("Platform", changed.getName());
        assertEquals("Mumbai", changed.getLocation());
    }

    private Department department(Integer id, String name, String location) {
        Department d = new Department();
        d.setId(id);
        d.setName(name);
        d.setLocation(location);
        return d;
    }
}
