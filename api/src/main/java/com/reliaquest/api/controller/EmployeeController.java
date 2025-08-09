package com.reliaquest.api.controller;

import com.reliaquest.api.exception.EmployeeCreationException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.ServerResponse;
import com.reliaquest.api.service.EmployeeCacheService;
import com.reliaquest.api.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Implementation of IEmployeeController interface.
 * Provides REST endpoints for employee management operations.
 * @see IEmployeeController
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController implements IEmployeeController<Employee, CreateEmployeeInput> {

    private final EmployeeService employeeService;
    private final EmployeeCacheService employeeCacheService;

    /**
     * Retrieves a list of all employees.
     *
     * @return ResponseEntity containing a list of all employees
     */
    @Override
    @GetMapping()
    public ResponseEntity<List<Employee>> getAllEmployees() {
        log.debug("Getting all employees");
        List<Employee> employees = employeeService.getAllEmployees().getData();
        return ResponseEntity.ok(employees);
    }

    /**
     * Searches for employees whose names contain the specified search string.
     *
     * @param searchString The string to search for in employee names
     * @return ResponseEntity containing a list of employees whose names contain the search string
     */
    @Override
    @GetMapping("/search/{searchString}")
    public ResponseEntity<List<Employee>> getEmployeesByNameSearch(@PathVariable String searchString) {
        log.debug("Searching employees by name containing: {}", searchString);
        List<Employee> matchingEmployees = employeeService.getEmployeesByNameSearch(searchString);
        return ResponseEntity.ok(matchingEmployees);
    }

    /**
     * Retrieves an employee by their ID.
     *
     * @param id Employee ID as a String (must be a valid UUID)
     * @return ResponseEntity containing the employee with the given ID
     * @throws IllegalArgumentException if the ID is not a valid UUID format
     * @throws EmployeeNotFoundException if the employee is not found
     */
    @Override
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable final String id) {
        log.debug("Getting employee by id: {}", id);

        // This may throw IllegalArgumentException which is handled by ControllerAdvice
        val idAsUUID = UUID.fromString(id);

        ServerResponse<Employee> response = employeeService.getEmployeeById(idAsUUID);

        Employee employee = response.getData();
        return ResponseEntity.ok(employee);
    }

    /**
     * Retrieves the highest salary among all employees.
     * Returns 0 if no employees are found or if no employee has a salary.
     *
     * @return ResponseEntity containing the highest salary as an integer
     */
    @Override
    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        log.debug("Getting highest salary of employees");
        Integer highestSalary = employeeService.getHighestSalaryOfEmployees();
        return ResponseEntity.ok(highestSalary);
    }

    /**
     * Retrieves the names of the top ten highest earning employees, sorted by salary in descending order.
     * Returns an empty list if no employees are found.
     * Only includes employees with non-null names and salaries.
     *
     * @return ResponseEntity containing a list of the top ten highest earning employee names
     */
    @Override
    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        log.debug("Getting top ten highest earning employee names");
        List<String> topEmployees = employeeService.getTopTenHighestEarningEmployeeNames();
        return ResponseEntity.ok(topEmployees);
    }

    /**
     * Creates a new employee with the provided information.
     *
     * @param employeeInput Employee input containing name, salary, and age
     * @return ResponseEntity containing the created Employee if successful
     * @throws EmployeeCreationException if employee creation fails
     */
    @Override
    @PostMapping()
    public ResponseEntity<Employee> createEmployee(@RequestBody CreateEmployeeInput employeeInput) {
        log.info("Creating employee with name: {}", employeeInput.getName());

        Employee createdEmployee = employeeService.createEmployee(employeeInput);
        if (createdEmployee != null) {
            log.info("Successfully created employee with id: {}", createdEmployee.getId());
            return ResponseEntity.ok(createdEmployee);
        } else {
            String errorMessage = "null response from service";
            log.error("Failed to create employee: {}", errorMessage);
            throw new EmployeeCreationException(errorMessage);
        }
    }

    /**
     * Deletes an employee by their ID.
     *
     * @param id Employee ID as a String (must be a valid UUID)
     * @return ResponseEntity containing the name of the deleted employee if successful,
     *         or a bad request with an error message if deletion fails
     * @throws IllegalArgumentException if the ID is not a valid UUID format
     */
    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        log.info("Deleting employee with id: {}", id);

        // This may throw IllegalArgumentException which is handled by ControllerAdvice
        val idAsUUID = UUID.fromString(id);
        String result = employeeService.deleteEmployeeById(idAsUUID);

        if (!result.equals("Failed to delete employee.")) {
            log.info("Successfully deleted employee");
            return ResponseEntity.ok(result);
        } else {
            log.error("Failed to delete employee");
            return ResponseEntity.badRequest().body(result);
        }
    }
}
