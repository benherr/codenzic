package com.example.employeemanagement.repository;

import com.example.employeemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    /**
     * Single query for the optional filtering of GET /api/v1/employees.
     */
    @Query("""
            SELECT e FROM Employee e
            WHERE (:keyword IS NULL
                   OR LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.lastName)  LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(e.email)     LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:department IS NULL OR LOWER(e.department) = LOWER(:department))
            """)
    org.springframework.data.domain.Page<Employee> search(
            @Param("keyword") String keyword,
            @Param("department") String department,
            org.springframework.data.domain.Pageable pageable);
}
