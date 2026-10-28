package com.example.employeemanagement.service;

import com.example.employeemanagement.dto.EmployeeRequest;
import com.example.employeemanagement.dto.EmployeeResponse;
import com.example.employeemanagement.dto.PageResponse;
import org.springframework.data.domain.Pageable;

/**
 * Business contract. The controller depends on this interface, not on the
 * implementation, which keeps the layers loosely coupled and testable.
 */
public interface EmployeeService {

    EmployeeResponse create(EmployeeRequest request);

    PageResponse<EmployeeResponse> findAll(String keyword, String department, Pageable pageable);

    EmployeeResponse findById(Long id);

    EmployeeResponse update(Long id, EmployeeRequest request);

    void delete(Long id);
}
