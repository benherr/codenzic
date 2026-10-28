package com.example.employeemanagement.exception;

/** Thrown when a requested employee does not exist -> HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException employee(Long id) {
        return new ResourceNotFoundException("Employee not found with id: " + id);
    }
}
