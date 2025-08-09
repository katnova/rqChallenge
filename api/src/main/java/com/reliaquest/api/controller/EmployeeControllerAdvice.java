package com.reliaquest.api.controller;

import com.reliaquest.api.exception.EmployeeCreationException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Controller advice for handling exceptions in the EmployeeController.
 */
@Slf4j
@ControllerAdvice(assignableTypes = EmployeeController.class)
public class EmployeeControllerAdvice {

    /**
     * Handles IllegalArgumentException, which is thrown when parsing invalid UUIDs.
     *
     * @param ex the exception
     * @return a ResponseEntity with a bad request status and an error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Invalid argument: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest()
                .header("X-Error-Message", "Invalid UUID format: " + ex.getMessage())
                .body("Invalid UUID format.");
    }

    /**
     * Handles EmployeeCreationException, which is thrown when employee creation fails.
     *
     * @param ex the exception
     * @return a ResponseEntity with a bad request status and an error message
     */
    @ExceptionHandler(EmployeeCreationException.class)
    protected ResponseEntity<?> handleEmployeeCreationException(EmployeeCreationException ex) {
        log.error("Employee creation failed: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest()
                .header("X-Error-Message", "Failed to create employee: " + ex.getMessage())
                .body(ex.getMessage());
    }

    /**
     * Handles EmployeeNotFoundException, which is thrown when an employee cannot be found.
     *
     * @param ex the exception
     * @return a ResponseEntity with a not found status and an error message
     */
    @ExceptionHandler(EmployeeNotFoundException.class)
    protected ResponseEntity<?> handleEmployeeNotFoundException(EmployeeNotFoundException ex) {
        log.error("Employee not found: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .header("X-Error-Message", "Employee not found: " + ex.getMessage())
                .body(ex.getMessage());
    }

    /**
     * Handles all other exceptions not specifically handled by other methods.
     * This is a fallback exception handler.
     *
     * @param ex the exception
     * @return a ResponseEntity with a bad request status and an error message
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleException(Exception ex) {
        log.error("Error handling employee request: {}", ex.getMessage(), ex);
        return ResponseEntity.badRequest()
                .header("X-Error-Message", "Error processing request: " + ex.getMessage())
                .build();
    }
}
