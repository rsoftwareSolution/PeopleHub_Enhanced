package com.demo.department.controller;

import com.demo.department.config.Constants;
import com.demo.department.model.Department;
import com.demo.department.service.DepartmentService;
import com.demo.department.client.EmployeeClient;
import com.demo.department.dto.DepartmentSummary;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.DEPARTMENTS)
public class DepartmentController {

    private final DepartmentService service;
    private final EmployeeClient employeeClient;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(Constants.SORT_NAME, Constants.SORT_LOCATION, Constants.SORT_ID);

    @GetMapping
    public List<Department> all() {
        return service.findAll();
    }

    @GetMapping(Constants.ID)
    public Department one(@PathVariable("id") Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Department create(@Valid @RequestBody Department item) {
        return service.save(item);
    }

    @PutMapping(Constants.ID)
    public Department update(@PathVariable("id") Integer id, @Valid @RequestBody Department item) {

        return service.update(id, item);
    }

    @DeleteMapping(Constants.ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id, @RequestHeader(Constants.AUTHORIZATION) String token) {
        service.findById(id);
        long employeeCount = employeeClient.employeeCount(id, token);

        if (employeeCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, Constants.DEPARTMENT_DELETE_ERROR);
        }
        service.delete(id);
    }

    @GetMapping(Constants.WITH_EMPLOYEE_COUNTS)
    public List<DepartmentSummary> withEmployeeCounts(@RequestHeader(Constants.AUTHORIZATION) String token) {

        Map<Integer, Long> counts = employeeClient.departmentCounts(token).stream().collect(Collectors.toMap(m ->
                ((Number) m.get(Constants.DEPARTMENT_ID)).intValue(), m -> ((Number) m.get(Constants.EMPLOYEE_COUNT)).longValue()));
        return service.findAll().stream().map(d -> new DepartmentSummary(d, counts.getOrDefault(d.getId(),
                Constants.DEFAULT_EMPLOYEE_COUNT))).toList();
    }

    @GetMapping(Constants.SEARCH)
    public Page<Department> search(@RequestParam(name = Constants.PARAM_Q, required = false) String q,
                                   @RequestParam(name = Constants.PARAM_PAGE, defaultValue = "0") int page,
                                   @RequestParam(name = Constants.PARAM_SIZE, defaultValue = "20") int size,
                                   @RequestParam(name = Constants.PARAM_SORT, defaultValue = "name,asc") String sort) {

        if (page < 0 || size < Constants.MIN_PAGE_SIZE || size > Constants.MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_PAGE_SIZE);
        }

        String[] parts = sort.split(",", 2);
        String sortField = parts[0].trim();

        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.UNSUPPORTED_SORT_FIELD);
        }

        String sortDirection = parts.length > 1 ? parts[1].trim() : Constants.SORT_ASC;

        if (!sortDirection.equalsIgnoreCase(Constants.SORT_ASC) && !sortDirection.equalsIgnoreCase(Constants.SORT_DESC)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_SORT_DIRECTION);
        }

        Sort.Direction direction = sortDirection.equalsIgnoreCase(Constants.SORT_DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return service.search(q, PageRequest.of(page, size, Sort.by(direction, sortField)));
    }
}
