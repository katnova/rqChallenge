package com.reliaquest.api.service;

import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.model.CreateEmployeeInput;
import com.reliaquest.api.model.DeleteEmployeeInput;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.ServerResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for managing employee data and operations.
 * This class serves as the primary business logic layer for employee-related functionality.
 * It coordinates between the cache service and the REST client to provide efficient data access
 * while ensuring data consistency.
 *
 * The service provides methods for retrieving, creating, and deleting employees,
 * as well as specialized operations like searching by name and finding highest salaries.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeCacheService employeeCacheService;
    private final EmployeeRestClient employeeRestClient;

    /**
     * Retrieves all employees from the system.
     *
     * This method delegates to the cache service to efficiently retrieve employee data,
     * which may either return cached data or fetch fresh data from the backend API.
     *
     * @return A ServerResponse containing a list of all employees and a status
     */
    public ServerResponse<List<Employee>> getAllEmployees() {
        return employeeCacheService.getAllEmployees();
    }

    /**
     * Searches for employees whose names contain the specified search string.
     *
     * This method first retrieves all employees and then filters the results to include
     * only those employees whose names contain the provided search string.
     * The search is case-sensitive.
     *
     * @param searchString The string to search for in employee names
     * @return A list of employees whose names contain the search string
     */
    public List<Employee> getEmployeesByNameSearch(final String searchString) {
        ServerResponse<List<Employee>> employees = getAllEmployees();
        if (employees == null
                || employees.getData() == null
                || employees.getData().isEmpty()) {
            return List.of();
        }
        return employees.getData().stream()
                .filter(employee ->
                        employee.getName() != null && employee.getName().contains(searchString))
                .toList();
    }

    /**
     * Retrieves an employee by their unique identifier.
     *
     * This method delegates to the cache service to efficiently retrieve the employee data,
     * which may either return cached data or fetch fresh data from the backend API.
     *
     * @param id The UUID of the employee to retrieve
     * @return A ServerResponse containing the employee with the specified ID and a status
     * @throws EmployeeNotFoundException if no employee with the specified ID exists
     */
    public ServerResponse<Employee> getEmployeeById(final UUID id) {
        return employeeCacheService.getEmployeeById(id);
    }

    /**
     * Finds the highest salary among all employees.
     *
     * This method retrieves all employees and then determines the maximum salary value.
     * If there are no employees or no employees with a salary, it returns 0.
     *
     * @return The highest salary value as an Integer, or 0 if no salaries are found
     */
    public Integer getHighestSalaryOfEmployees() {
        ServerResponse<List<Employee>> employees = getAllEmployees();
        if (employees == null
                || employees.getData() == null
                || employees.getData().isEmpty()) {
            return 0;
        }
        return employees.getData().stream()
                .map(Employee::getSalary)
                .filter(salary -> salary != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * Retrieves the names of the top ten highest earning employees.
     *
     * This method retrieves all employees, sorts them by salary in descending order,
     * takes the top ten, and returns their names. Employees without a salary are excluded.
     * If there are fewer than ten employees with salaries, all of them are returned.
     *
     * @return A list of employee names, sorted by their salaries in descending order, limited to ten entries
     */
    public List<String> getTopTenHighestEarningEmployeeNames() {
        ServerResponse<List<Employee>> employees = getAllEmployees();
        if (employees == null
                || employees.getData() == null
                || employees.getData().isEmpty()) {
            return List.of();
        }
        return employees.getData().stream()
                .filter(employee -> employee.getSalary() != null)
                .sorted((e1, e2) -> e2.getSalary().compareTo(e1.getSalary()))
                .limit(10)
                .map(Employee::getName)
                .filter(name -> name != null && !name.isEmpty())
                .toList();
    }

    /**
     * Creates a new employee with the provided information.
     *
     * This method sends a request to create a new employee via the REST client.
     * If the creation is successful, the new employee is also added to the cache
     * to ensure cache consistency.
     *
     * @param input The employee information to create a new employee
     * @return The newly created Employee object, or null if creation failed
     */
    public Employee createEmployee(final CreateEmployeeInput input) {
        try {
            ServerResponse<Employee> response = employeeRestClient.createEmployee(input);
            if (response != null && response.getData() != null) {
                employeeCacheService.putEmployee(response.getData());
                return response.getData();
            } else {
                return null;
            }

        } catch (Exception e) {
            log.error("Error creating employee: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Deletes an employee by their unique identifier.
     *
     * This method first retrieves the employee to get their name, then sends a delete request
     * to the REST client using the employee's name. If the deletion is successful, the employee
     * is also removed from the cache to ensure cache consistency.
     *
     * @param uuid The UUID of the employee to delete
     * @return The name of the deleted employee if successful, or an error message if the deletion failed
     */
    public String deleteEmployeeById(final UUID uuid) {
        try {
            ServerResponse<Employee> employeeResponse = getEmployeeById(uuid);

            if (employeeResponse != null && employeeResponse.getData() != null) {
                Employee employee = employeeResponse.getData();

                if (employee.getName() != null && !employee.getName().isEmpty()) {
                    String employeeName = employee.getName();
                    DeleteEmployeeInput input = new DeleteEmployeeInput();
                    input.setName(employeeName);

                    ServerResponse<Boolean> response = employeeRestClient.deleteEmployee(input);
                    if (response != null && response.getData() != null && response.getData()) {
                        employeeCacheService.evictEmployee(uuid);
                        return employeeName;
                    }
                }
            }
        } catch (EmployeeNotFoundException ignored) {
        }
        return "Failed to delete employee.";
    }
}
