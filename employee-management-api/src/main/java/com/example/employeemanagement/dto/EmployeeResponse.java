package com.example.employeemanagement.dto;

import java.math.BigDecimal;

public record EmployeeResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String department,
        String position,
        BigDecimal salary
) {
}
