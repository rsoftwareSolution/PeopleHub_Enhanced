package com.demo.employee.repository;

import com.demo.employee.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByDepartmentIdOrderByNameAsc(Integer departmentId);

    long countByDepartmentId(Integer departmentId);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Integer id);

    @Query("select e from Employee e where (:departmentId is null or e.departmentId = :departmentId) " +
            "and (:query is null or lower(e.name) like lower(concat('%', :query, '%')) " +
            "or lower(e.email) like lower(concat('%', :query, '%')))")
    Page<Employee> searchEmployees(@Param("query") String query, @Param("departmentId") Integer departmentId, Pageable pageable);

    @Query("select e.departmentId, count(e) from Employee e group by e.departmentId")
    List<Object[]> countEmployeesByDepartment();
}
