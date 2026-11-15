package com.demo.employee.controller;

import com.demo.employee.client.DepartmentClient;
import com.demo.employee.config.Constants;
import com.demo.employee.model.Employee;
import com.demo.employee.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(Constants.EMPLOYEES)
public class EmployeeController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(Constants.SORT_NAME, Constants.SORT_EMAIL, Constants.SORT_ID, Constants.SORT_DEPARTMENT_ID);

    private final EmployeeService service;
    private final DepartmentClient departmentClient;

    @GetMapping
    public List<Employee> all() {
        return service.findAll();
    }

    @GetMapping(Constants.ID)
    public Employee one(@PathVariable("id") Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Employee create(@Valid @RequestBody Employee item, @RequestHeader(Constants.AUTHORIZATION) String token) {
        departmentClient.getDepartment(item.getDepartmentId().longValue(), token);
        return service.save(item);
    }

    @PutMapping(Constants.ID)
    public Employee update(@PathVariable("id") Integer id, @Valid @RequestBody Employee item,
                           @RequestHeader(Constants.AUTHORIZATION) String token) {
        departmentClient.getDepartment(item.getDepartmentId().longValue(), token);
        return service.update(id, item);
    }

    @DeleteMapping(Constants.ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Integer id) {
        service.delete(id);
    }

    @GetMapping(Constants.SEARCH)
    public Page<Employee> search(@RequestParam(name = Constants.PARAM_Q, required = false) String q,
                                 @RequestParam(name = Constants.PARAM_DEPARTMENT_ID, required = false) Integer departmentId,
                                 @RequestParam(name = Constants.PARAM_PAGE, defaultValue = Constants.DEFAULT_PAGE_VALUE) int page,
                                 @RequestParam(name = Constants.PARAM_SIZE, defaultValue = Constants.DEFAULT_SIZE_VALUE) int size,
                                 @RequestParam(name = Constants.PARAM_SORT, defaultValue = Constants.DEFAULT_SORT) String sort) {

        if (page < 0 || size < Constants.MIN_PAGE_SIZE || size > Constants.MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_PAGE_SIZE);
        }

        String[] sortParts = getSortString(sort);
        Sort.Direction direction = sortParts[1].equalsIgnoreCase(Constants.SORT_DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return service.search(q, departmentId, PageRequest.of(page, size, Sort.by(direction, sortParts[0])));
    }

    private static String[] getSortString(String sort) {

        if (sort == null || sort.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.UNSUPPORTED_SORT_FIELD);
        }

        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        String direction = parts.length > 1 ? parts[1].trim() : Constants.DEFAULT_SORT_DIRECTION;

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.UNSUPPORTED_SORT_FIELD);
        }

        if (!direction.equalsIgnoreCase(Constants.SORT_ASC) && !direction.equalsIgnoreCase(Constants.SORT_DESC)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.INVALID_SORT_DIRECTION);
        }
        return new String[]{field, direction};
    }
}
