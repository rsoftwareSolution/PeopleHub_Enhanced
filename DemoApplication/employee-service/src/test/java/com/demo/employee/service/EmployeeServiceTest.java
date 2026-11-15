package com.demo.employee.service;

import com.demo.employee.model.Employee;
import com.demo.employee.repository.EmployeeRepository;
import com.demo.employee.exception.ResourceNotFoundException;
import com.demo.employee.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock
    EmployeeRepository repository;
    @InjectMocks
    EmployeeService service;

    @Test
    void findByIdReturnsEntity() {
        Employee item = employee(7, "Asha", "asha@example.com", 3);
        when(repository.findById(7)).thenReturn(Optional.of(item));
        assertEquals(7, service.findById(7).getId());
    }

    @Test
    void missingEntityThrows() {
        when(repository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findById(99));
    }

    @Test
    void createNormalizesEmailAndPersists() {
        Employee item = employee(null, "Asha", " ASHA@EXAMPLE.COM ", 3);
        when(repository.existsByEmailIgnoreCase("asha@example.com")).thenReturn(false);
        when(repository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));
        Employee saved = service.save(item);
        assertEquals("asha@example.com", saved.getEmail());
        verify(repository).save(item);
    }

    @Test
    void duplicateEmailIsRejected() {
        Employee item = employee(null, "Asha", "asha@example.com", 3);
        when(repository.existsByEmailIgnoreCase("asha@example.com")).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.save(item));
        verify(repository, never()).save(any());
    }

    @Test
    void updatePreservesIdAndChangesDepartment() {
        Employee existing = employee(4, "Asha", "asha@example.com", 1);
        when(repository.findById(4)).thenReturn(Optional.of(existing));
        when(repository.existsByEmailIgnoreCaseAndIdNot("asha@example.com", 4)).thenReturn(false);
        when(repository.save(existing)).thenReturn(existing);
        Employee updated = service.update(4, employee(null, "Asha Rao", "asha@example.com", 2));
        assertEquals(4, updated.getId());
        assertEquals(2, updated.getDepartmentId());
        assertEquals("Asha Rao", updated.getName());
    }

    @Test
    void emptyDepartmentHasZeroEmployees() {
        when(repository.countByDepartmentId(90)).thenReturn(0L);
        assertEquals(0, service.countByDepartment(90));
    }

    @Test
    void employeesAreLoadedByDepartment() {
        when(repository.findByDepartmentIdOrderByNameAsc(3)).thenReturn(List.of(employee(1, "A", "a@x.com", 3)));
        assertEquals(1, service.findByDepartment(3).size());
    }

    private Employee employee(Integer id, String name, String email, Integer department) {
        Employee e = new Employee();
        e.setId(id);
        e.setName(name);
        e.setEmail(email);
        e.setDepartmentId(department);
        return e;
    }
}
