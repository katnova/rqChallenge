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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Client for interacting with the employee REST API.
 * This class provides methods to perform CRUD operations on employee resources.
 * It uses Spring's RestClient to make HTTP requests to the underlying employee service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeRestClient {

    private final RestClient restClient;

    /**
     * Retrieves all employees from the server.
     *
     * @return A ServerResponse containing a list of all employees
     */
    public ServerResponse<List<Employee>> getAllEmployees() {
        log.trace("Getting all employees...");
        return restClient.get().uri("").retrieve().body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Retrieves an employee by their unique identifier.
     *
     * @param id The UUID of the employee to retrieve
     * @return A ServerResponse containing the employee with the specified ID
     * @throws EmployeeNotFoundException if no employee with the specified ID exists
     */
    public ServerResponse<Employee> getEmployeeById(final UUID id) {
        log.trace("Getting employee with id {}...", id);
        return restClient
                .get()
                .uri("/{id}", id)
                .retrieve()
                .onStatus(status -> status == HttpStatus.NOT_FOUND, (request, response) -> {
                    throw new EmployeeNotFoundException();
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Creates a new employee with the provided information.
     *
     * @param input The employee information to create a new employee
     * @return A ServerResponse containing the newly created employee
     */
    public ServerResponse<Employee> createEmployee(final CreateEmployeeInput input) {
        log.trace("Creating employee with name {}...", input.getName());
        return restClient
                .post()
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(input)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    /**
     * Deletes an employee with the specified name.
     *
     * @param input The input containing the name of the employee to delete
     * @return A ServerResponse containing a boolean indicating whether the deletion was successful
     */
    public ServerResponse<Boolean> deleteEmployee(final DeleteEmployeeInput input) {
        log.trace("Deleting employee with name {}...", input.getName());
        return restClient
                .method(org.springframework.http.HttpMethod.DELETE)
                .uri("")
                .contentType(MediaType.APPLICATION_JSON)
                .body(input)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
